package cvetkovic.x64;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleIntegerConst;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.semantics.ClassMetadata;
import cvetkovic.x64.cpu.Descriptor;
import cvetkovic.x64.cpu.RegisterDescriptor;
import cvetkovic.x64.cpu.ResourceManager;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MachineCodeGenerator {

    private static final String[][] registerNames = {
            new String[]{"rax", "eax", "al"},
            new String[]{"rbx", "ebx", "bl"},
            new String[]{"rcx", "ecx", "cl"},
            new String[]{"rdx", "edx", "dl"},
            new String[]{"rdi", "edi", "dil"},
            new String[]{"rsi", "esi", "sil"}};

    private String outputFileUrl;
    private File outputFileHandle;
    private BufferedWriter writer;

    private int instructionCounter = 0;

    private List<CodeSequence> codeSequences;
    private Set<Obj> globalVariables;
    private List<ClassMetadata> classMetadata;
    private ResourceManager resourceManager;

    private static final String integerTypeLabel = "number_format";
    private static final String nonIntegerTypeLabel = "character_format";

    private List<RegisterDescriptor> registers = new ArrayList<>();
    private Map<String, RegisterDescriptor> mapToRegister = new HashMap<>();

    /**
     * Just does variable assignments to class fields. Invoke generateCode() method to do the compilation.
     *
     * @param outputFileUrl
     * @param codeSequences
     * @param globalVariables
     * @param classMetadata
     */
    public MachineCodeGenerator(String outputFileUrl, List<CodeSequence> codeSequences,
                                Set<Obj> globalVariables, List<ClassMetadata> classMetadata) {
        this.outputFileUrl = outputFileUrl;
        this.codeSequences = codeSequences;
        this.globalVariables = globalVariables;
        this.classMetadata = classMetadata;
    }

    /**
     * Generates assembly code for MicroJava program
     */
    public void generateCode() {
        try {
            outputFileHandle = new File(outputFileUrl);
            outputFileHandle.createNewFile();

            writer = new BufferedWriter(new FileWriter(outputFileHandle));

            generateDirectives();
            generateBSS();
            generatePolymorphismTables();
            generateFunctionsBody();

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes register and memory descriptors
     */
    private void initializeISATables(BasicBlock basicBlock) {
        registers.clear();
        mapToRegister.clear();

        for (String[] id : registerNames) {
            RegisterDescriptor newRegister = new RegisterDescriptor(id[2], id[1], id[0]);

            registers.add(newRegister);
            for (int i = 0; i < 3; i++)
                mapToRegister.put(id[i], newRegister);
        }

        List<BasicBlock.Tuple<Obj, Boolean>> memoryLocationList = new ArrayList<>();
        for (Obj var : basicBlock.allVariables)
            memoryLocationList.add(new BasicBlock.Tuple<>(var, globalVariables.contains(var)));

        resourceManager = new ResourceManager(registers, memoryLocationList);
    }

    /**
     * Generates Intel syntax directive and exports 'main' symbol
     *
     * @throws IOException
     */
    private void generateDirectives() throws IOException {
        writer.write(".intel_syntax noprefix");
        writer.write(System.lineSeparator());

        writer.write(".extern calloc");
        writer.write(System.lineSeparator());
        writer.write(".extern printf");
        writer.write(System.lineSeparator());
        writer.write(".extern scanf");
        writer.write(System.lineSeparator());

        writer.write(".global main");
        writer.write(System.lineSeparator());
        writer.write(System.lineSeparator());
    }

    /**
     * Allocates places for uninitialized global variables
     *
     * @throws IOException
     */
    private void generateBSS() throws IOException {
        if (globalVariables.size() > 0) {
            writer.write(".section .bss");
            writer.write(System.lineSeparator());

            for (Obj var : globalVariables) {
                writer.write(var.getName() + ":");
                writer.write(System.lineSeparator());
                writer.write("\t" + SystemV_ABI.getAssemblyDirectiveForAllocation(SystemV_ABI.getX64VariableSize(var.getType())) + " 0x0");
                writer.write(System.lineSeparator());
            }

            writer.write(System.lineSeparator());
            writer.write(System.lineSeparator());
        }
    }

    private void generatePolymorphismTables() throws IOException {
        writer.write(".section .data");
        writer.write(System.lineSeparator());

        // scanf/print character format
        writer.write(nonIntegerTypeLabel + ":");
        writer.write(System.lineSeparator());
        writer.write("\t.asciz \"%c\"");
        writer.write(System.lineSeparator());

        // scanf/print number format
        writer.write(integerTypeLabel + ":");
        writer.write(System.lineSeparator());
        writer.write("\t.asciz \"%d\"");
        writer.write(System.lineSeparator());

        if (classMetadata.size() > 0) {
            StringBuilder table = new StringBuilder();

            for (ClassMetadata metadata : classMetadata) {
                table.append("; VFT for ").append(metadata.className).append("\n");
                metadata.pointersToFunction.forEach((n, p) -> table.append(n).append(":\n\t.quad ").append(p.getName()));
                table.append(System.lineSeparator());
            }

            writer.write(table.toString());
        }

        writer.write(System.lineSeparator());
    }

    private boolean checkEquality(int a, int b) {
        return a == b;
    }

    /**
     * Generates assembly code for methods' body
     *
     * @throws IOException
     */
    private void generateFunctionsBody() throws IOException {
        writer.write(".section .text");
        writer.write(System.lineSeparator());

        for (CodeSequence function : codeSequences) {
            BasicBlock basicBlock = null;

            for (int i = 0; i < function.basicBlocks.size(); i++) {
                if (basicBlock == null) {
                    // assign new basic block to compile
                    if (instructionCounter == 0)
                        basicBlock = function.entryBlock;
                    else
                        basicBlock = function.basicBlocks.stream().filter(p -> checkEquality(p.firstQuadruple, instructionCounter)).collect(Collectors.toList()).get(0);
                }

                List<String> aux = new ArrayList<>();
                boolean cancelSaveDirtyVals = false;

                // old register/memory descriptors are discarded
                initializeISATables(basicBlock);

                for (Quadruple quadruple : basicBlock.instructions) {
                    Obj obj1 = (quadruple.getArg1() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) quadruple.getArg1()).getObj() : null);
                    Obj obj2 = (quadruple.getArg2() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) quadruple.getArg2()).getObj() : null);
                    Obj objResult = (quadruple.getResult() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) quadruple.getResult()).getObj() : null);

                    int numOfArgsInMemory = 0;

                    switch (quadruple.getInstruction()) {
                        //////////////////////////////////////////////////////////////////////////////////
                        // ARITHMETIC INSTRUCTION (allowed only on int type in MikroJava - 32-bit)
                        //////////////////////////////////////////////////////////////////////////////////

                        case ADD: {
                            // if first instruction is constant then load it to register first
                            // if second operand is constant then encode it in instruction
                            RegisterDescriptor dest_arg1_register = resourceManager.getRegister(obj1, quadruple);
                            RegisterDescriptor arg2_register = (obj2.getKind() != Obj.Con ? resourceManager.getRegister(obj2, quadruple) : null);

                            dest_arg1_register.setPrintWidth(4);
                            resourceManager.fetchOperand(dest_arg1_register, obj1, aux);

                            if (arg2_register != null && arg2_register.getHoldsValueOf() != obj2)
                                numOfArgsInMemory++;
                            else if (arg2_register != null)
                                resourceManager.fetchOperand(arg2_register, obj2, aux);

                            resourceManager.invalidate(dest_arg1_register, objResult, aux);
                            resourceManager.validate(dest_arg1_register, objResult, aux, true);

                            issueAuxiliaryInstructions(aux);
                            String secondOperand;
                            if (arg2_register == null)
                                secondOperand = String.valueOf(obj2.getAdr());
                            else if (numOfArgsInMemory > 0)
                                secondOperand = resourceManager.getAddressDescriptor(obj2).toString();
                            else
                                secondOperand = arg2_register.toString();

                            writer.write("\tADD " + dest_arg1_register + ", " + secondOperand);
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case SUB: {
                            // if first instruction is constant then load it to register first
                            // if second operand is constant then encode it in instruction
                            RegisterDescriptor dest_arg1_register = resourceManager.getRegister(obj1, quadruple);
                            RegisterDescriptor arg2_register = (obj2.getKind() != Obj.Con ? resourceManager.getRegister(obj2, quadruple) : null);

                            dest_arg1_register.setPrintWidth(4);
                            resourceManager.fetchOperand(dest_arg1_register, obj1, aux);

                            if (arg2_register != null && arg2_register.getHoldsValueOf() != obj2)
                                numOfArgsInMemory++;
                            else if (arg2_register != null)
                                resourceManager.fetchOperand(arg2_register, obj2, aux);

                            resourceManager.invalidate(dest_arg1_register, objResult, aux);
                            resourceManager.validate(dest_arg1_register, objResult, aux, true);

                            issueAuxiliaryInstructions(aux);
                            String secondOperand;
                            if (arg2_register == null)
                                secondOperand = String.valueOf(obj2.getAdr());
                            else if (numOfArgsInMemory > 0)
                                secondOperand = resourceManager.getAddressDescriptor(obj2).toString();
                            else
                                secondOperand = arg2_register.toString();

                            writer.write("\tSUB " + dest_arg1_register + ", " + secondOperand);
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case MUL: {
                            // if first instruction is constant then load it to register first
                            // if second operand is constant then encode it in instruction
                            RegisterDescriptor dest_arg1_register = resourceManager.getRegister(obj1, quadruple);
                            RegisterDescriptor arg2_register = (obj2.getKind() != Obj.Con ? resourceManager.getRegister(obj2, quadruple) : null);

                            dest_arg1_register.setPrintWidth(4);
                            resourceManager.fetchOperand(dest_arg1_register, obj1, aux);

                            if (arg2_register != null && arg2_register.getHoldsValueOf() != obj2)
                                numOfArgsInMemory++;
                            else if (arg2_register != null)
                                resourceManager.fetchOperand(arg2_register, obj2, aux);

                            resourceManager.invalidate(dest_arg1_register, objResult, aux);
                            resourceManager.validate(dest_arg1_register, objResult, aux, true);

                            issueAuxiliaryInstructions(aux);
                            String secondOperand;
                            if (arg2_register == null)
                                secondOperand = String.valueOf(obj2.getAdr());
                            else if (numOfArgsInMemory > 0)
                                secondOperand = resourceManager.getAddressDescriptor(obj2).toString();
                            else
                                secondOperand = arg2_register.toString();

                            writer.write("\tIMUL " + dest_arg1_register + ", " + secondOperand);
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case DIV: {
                            /*RegisterDescriptor source = null; //("rax", obj1);
                            RegisterDescriptor divideBy = null;

                            writer.write("\tmovsx eax, " + source);
                            writer.write(System.lineSeparator());
                            writer.write("\tcdq"); // TODO: AH -> EAX sign extension
                            writer.write(System.lineSeparator());
                            writer.write("\tidiv " + divideBy);
                            writer.write(System.lineSeparator());

                            // EAX stores the result

                            break;*/
                        }

                        case REM: {

                            // EDX stores the result

                            break;
                        }

                        case NEG: {
                            RegisterDescriptor zeroRegister = resourceManager.getRegisterByForce();
                            RegisterDescriptor source = resourceManager.getRegister(obj1, quadruple);

                            if (source.getHoldsValueOf() != obj1)
                                resourceManager.fetchOperand(source, obj1, aux);

                            resourceManager.invalidate(zeroRegister, null, aux);
                            resourceManager.validate(zeroRegister, objResult, aux, true);
                            issueAuxiliaryInstructions(aux);

                            writer.write("\tXOR " + zeroRegister + ", " + zeroRegister);
                            writer.write(System.lineSeparator());
                            zeroRegister.setPrintWidth(SystemV_ABI.getX64VariableSize(obj1.getType()));
                            if (source != null)
                                source.setPrintWidth(SystemV_ABI.getX64VariableSize(obj1.getType()));
                            writer.write("\tSUB " + zeroRegister + ", " + (source != null ? source : obj2));
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case STORE: {
                            RegisterDescriptor arg1_result_register = resourceManager.getRegister(objResult, quadruple);

                            resourceManager.fetchOperand(arg1_result_register, obj1, aux);

                            // optimization not to use new register to hold the value
                            RegisterDescriptor result_register = resourceManager.getRegister(objResult, quadruple);
                            if (result_register.getHoldsValueOf() == objResult)
                                arg1_result_register = result_register;

                            // change the address descriptor of destination register so that it only holds obj1 value
                            resourceManager.invalidate(arg1_result_register, objResult, aux);
                            resourceManager.validate(arg1_result_register, objResult, aux, true);

                            issueAuxiliaryInstructions(aux);

                            break;
                        }

                        case ENTER: {
                            writer.write("\tPUSH rbp");
                            writer.write(System.lineSeparator());
                            writer.write("\tMOV rbp, rsp");
                            writer.write(System.lineSeparator());

                            QuadrupleIntegerConst allocateSize = (QuadrupleIntegerConst) quadruple.getArg1();
                            int sizeToAllocate = allocateSize.getValue() + resourceManager.getSizeOfTempVars();
                            giveAddressToTemps(basicBlock, allocateSize.getValue());

                            // has to be divisible by 16 by System V ABI
                            writer.write("\tSUB rsp, " + SystemV_ABI.alignTo16(sizeToAllocate));
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case LEAVE: {
                            // save dirty variables
                            resourceManager.saveDirtyVariables(aux, false);
                            issueAuxiliaryInstructions(aux);
                            aux.clear();
                            cancelSaveDirtyVals = true;

                            writer.write("\tLEAVE");
                            writer.write(System.lineSeparator());
                            writer.write("\tRET");
                            writer.write(System.lineSeparator());

                            break;
                        }

                        /*case RETURN: {
                            RegisterDescriptor returnValueRegister = resourceManager.getRegisterSpecific("rax");
                            Descriptor source = resourceManager.getRegister(obj1, aux);

                            issueAuxiliaryInstructions(aux);
                            writer.write("\tmov " + returnValueRegister + ", " + (source != null ? source : obj2));
                            writer.write(System.lineSeparator());

                            resourceManager.invalidateFromRegister(returnValueRegister, aux);
                            resourceManager.validate(objResult, returnValueRegister, true);

                            break;
                        }*/

                        //////////////////////////////////////////////////////////////////////////////////
                        // INPUT / OUTPUT
                        //////////////////////////////////////////////////////////////////////////////////

                        case SCANF: {
                            Descriptor destination = resourceManager.getAddressDescriptor(objResult);

                            List<RegisterDescriptor> toPreserve = new ArrayList<>();
                            makeRegisterPreservationList(toPreserve);
                            resourceManager.preserveContext(toPreserve, aux);
                            issueAuxiliaryInstructions(aux);

                            // print format -> equivalent with mov rdi, offset FORMAT
                            writer.write("\tlea rdi, [rip + " + (objResult.getType().getKind() == Struct.Int ? integerTypeLabel : nonIntegerTypeLabel) + "]");
                            writer.write(System.lineSeparator());
                            // obj
                            writer.write("\tlea rsi, " + destination);
                            writer.write(System.lineSeparator());
                            // clear eax -> for variable number of vector registers
                            writer.write("\txor eax, eax");
                            writer.write(System.lineSeparator());
                            // invoke
                            writer.write("\tcall scanf");
                            writer.write(System.lineSeparator());

                            aux.clear();
                            resourceManager.restoreContext(toPreserve, aux);
                            issueAuxiliaryInstructions(aux);

                            break;
                        }

                        case PRINTF: {
                            RegisterDescriptor source = resourceManager.getRegister(obj2, quadruple);

                            resourceManager.invalidate(source, obj2, aux);

                            List<String> tmp = new ArrayList<>();
                            List<RegisterDescriptor> toPreserve = new ArrayList<>();
                            resourceManager.fetchOperand(source, obj2, tmp);
                            makeRegisterPreservationList(toPreserve);

                            // obj
                            int sourceSize;
                            if (source.getHoldsValueOf() != null)
                                sourceSize = SystemV_ABI.getX64VariableSize(source.getHoldsValueOf().getType());
                            else
                                sourceSize = 1; // char

                            String regName;
                            switch (sourceSize) {
                                case 1:
                                    regName = "sil";
                                    break;
                                case 4:
                                    regName = "esi";
                                    break;
                                case 8:
                                    regName = "rsi";
                                    break;
                                default:
                                    throw new RuntimeException("Data width not supported by print language construct.");
                            }

                            aux.addAll(tmp);
                            resourceManager.preserveContext(toPreserve, aux);
                            issueAuxiliaryInstructions(aux);

                            // data to be printed
                            if (source.getHoldsValueOf() != null || obj2.getKind() == Obj.Con) { // otherwise operand will be fetched by fetchOperandInstruction
                                source.setPrintWidth(sourceSize);
                                writer.write("\tMOV " + regName + ", " + source);
                                writer.write(System.lineSeparator());
                            }
                            // print format -> equivalent with mov rdi, offset FORMAT
                            writer.write("\tLEA rdi, [rip + " + (obj2.getType().getKind() == Struct.Int ? integerTypeLabel : nonIntegerTypeLabel) + "]");
                            writer.write(System.lineSeparator());
                            // clear eax -> for variable number of vector registers
                            writer.write("\tXOR eax, eax");
                            writer.write(System.lineSeparator());
                            // invoke
                            writer.write("\tCALL printf");
                            writer.write(System.lineSeparator());

                            aux.clear();
                            resourceManager.restoreContext(toPreserve, aux);
                            issueAuxiliaryInstructions(aux);

                            break;
                        }

                        //////////////////////////////////////////////////////////////////////////////////
                        // BRANCHES AND LABEL GENERATING
                        //////////////////////////////////////////////////////////////////////////////////

                        case JMP: {
                            // save dirty variables
                            resourceManager.saveDirtyVariables(aux, false);
                            issueAuxiliaryInstructions(aux);
                            aux.clear();
                            cancelSaveDirtyVals = true;

                            writer.write("\tjmp " + quadruple.getResult());
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case JL:
                        case JLE:
                        case JG:
                        case JGE:
                        case JE:
                        case JNE: {
                            // if first instruction is constant then load it to register first
                            // if second operand is constant then encode it in instruction
                            RegisterDescriptor dest_arg1_register = resourceManager.getRegister(obj1, quadruple);
                            RegisterDescriptor arg2_register = (obj2.getKind() != Obj.Con ? resourceManager.getRegister(obj2, quadruple) : null);

                            dest_arg1_register.setPrintWidth(4);
                            resourceManager.fetchOperand(dest_arg1_register, obj1, aux);

                            if (arg2_register != null && arg2_register.getHoldsValueOf() != obj2)
                                numOfArgsInMemory++;
                            else if (arg2_register != null)
                                resourceManager.fetchOperand(arg2_register, obj2, aux);

                            resourceManager.invalidate(dest_arg1_register, objResult, aux);
                            resourceManager.validate(dest_arg1_register, objResult, aux, true);

                            issueAuxiliaryInstructions(aux);
                            String secondOperand;
                            if (arg2_register == null)
                                secondOperand = String.valueOf(obj2.getAdr());
                            else if (numOfArgsInMemory > 0)
                                secondOperand = resourceManager.getAddressDescriptor(obj2).toString();
                            else
                                secondOperand = arg2_register.toString();

                            dest_arg1_register.setPrintWidth(SystemV_ABI.getX64VariableSize(obj1.getType()));
                            writer.write("\tCMP " + dest_arg1_register + ", " + secondOperand);
                            writer.write(System.lineSeparator());
                            String x64Instruction;
                            switch (quadruple.getInstruction()) {
                                case JL: {
                                    x64Instruction = "JL";
                                    break;
                                }
                                case JLE: {
                                    x64Instruction = "JLE";
                                    break;
                                }
                                case JG: {
                                    x64Instruction = "JG";
                                    break;
                                }
                                case JGE: {
                                    x64Instruction = "JGE";
                                    break;
                                }
                                case JE: {
                                    x64Instruction = "JE";
                                    break;
                                }
                                case JNE: {
                                    x64Instruction = "JNE";
                                    break;
                                }
                                default:
                                    throw new RuntimeException("Not supported jump instruction.");
                            }

                            // save dirty variables
                            aux.clear();
                            resourceManager.saveDirtyVariables(aux, false);
                            issueAuxiliaryInstructions(aux);
                            aux.clear();
                            cancelSaveDirtyVals = true;

                            writer.write("\t" + x64Instruction + " " + quadruple.getResult());
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case GEN_LABEL: {
                            writer.write(quadruple.getArg1().toString() + ":");
                            writer.write(System.lineSeparator());
                            break;
                        }

                        //////////////////////////////////////////////////////////////////////////////////
                        // OTHER
                        //////////////////////////////////////////////////////////////////////////////////

                        default:
                            throw new RuntimeException("Instruction not supported by x86-64 code generator.");
                    }

                    aux.clear();

                    instructionCounter++;
                }

                if (!cancelSaveDirtyVals) {
                    resourceManager.saveDirtyVariables(aux, false);
                    issueAuxiliaryInstructions(aux);
                    aux.clear();
                }

                basicBlock = null;

                // TODO: resourceManager.saveDirtyVariables(aux);
                // TODO: issueAuxiliaryInstructions(aux);
                // TODO: resourceManager.saveContext();
                // TODO: writer.write( machine code );
                // TODO: resourceManager.restoreContext();
            }
        }
    }

    private void makeRegisterPreservationList(List<RegisterDescriptor> toPreserve) {
        makeRegisterPreservationList(toPreserve, null);
    }

    private void makeRegisterPreservationList(List<RegisterDescriptor> toPreserve, RegisterDescriptor reg) {
        for (int i = 0; i < registerNames.length; i++) {
            /*if (reg != null) {
                String regName = reg.getWidest();
                if (regName == registerNames[i][0] || regName == registerNames[1][0]) // ebx isn't preserved
                    continue;
            }*/

            if (resourceManager.checkIfRegisterIsTaken(mapToRegister.get(registerNames[i][0])))
                toPreserve.add(mapToRegister.get(registerNames[i][0]));
        }
    }

    private void giveAddressToTemps(BasicBlock basicBlock, int startValue) {
        Collection<Obj> tempVars = basicBlock.temporaryVariables;
        for (Obj obj : tempVars) {
            if (obj.tempVar && obj.getKind() != Obj.Con) {
                int objSize = SystemV_ABI.getX64VariableSize(obj.getType());
                obj.setAdr(startValue + objSize);
                startValue += objSize;
            }
        }
    }

    private void issueAuxiliaryInstructions(List<String> aux) throws IOException {
        for (String line : aux) {
            writer.write(line);
            writer.write(System.lineSeparator());
        }
    }
}
