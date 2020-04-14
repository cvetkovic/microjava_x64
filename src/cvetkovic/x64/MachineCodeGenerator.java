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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    private List<CodeSequence> codeSequences;
    private Set<Obj> globalVariables;
    private List<ClassMetadata> classMetadata;
    private ResourceManager resourceManager;

    private static final String integerTypeLabel = "number_format";
    private static final String nonIntegerTypeLabel = "character_format";

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
        List<RegisterDescriptor> registers = new ArrayList<>();
        for (String[] id : registerNames)
            registers.add(new RegisterDescriptor(id[2], id[1], id[0]));

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

    /**
     * Generates assembly code for methods' body
     *
     * @throws IOException
     */
    private void generateFunctionsBody() throws IOException {
        writer.write(".section .text");
        writer.write(System.lineSeparator());

        for (CodeSequence function : codeSequences) {
            for (BasicBlock basicBlock : function.basicBlocks) {
                List<String> aux = new ArrayList<>();

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
                            /*boolean operandsSwapped = false;
                            Descriptor destAndArg1 = resourceManager.getRegister(obj1);
                            if (destAndArg1 == null) {
                                destAndArg1 = resourceManager.getRegister(obj2);
                                operandsSwapped = true;
                            }
                            Descriptor arg2 = (!operandsSwapped ? resourceManager.getRegister(obj2, aux) : resourceManager.getRegister(obj1, aux));

                            resourceManager.invalidateFromRegister(destAndArg1, aux);
                            resourceManager.validate(objResult, destAndArg1, true);

                            issueAuxiliaryInstructions(aux);
                            writer.write("\tadd " + destAndArg1 + ", " + (arg2 != null ? arg2 : obj2));
                            writer.write(System.lineSeparator());
*/
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
                            /*boolean operandsSwapped = false;
                            Descriptor destAndArg1 = resourceManager.getRegister(obj1, aux);
                            if (destAndArg1 == null) {
                                destAndArg1 = resourceManager.getRegister(obj2, aux);
                                operandsSwapped = true;
                            }
                            Descriptor arg2 = (!operandsSwapped ? resourceManager.getRegister(obj2, aux, true) : resourceManager.getRegister(obj1, aux, true));

                            resourceManager.invalidateFromRegister(destAndArg1, aux);
                            resourceManager.validate(objResult, destAndArg1, true);

                            issueAuxiliaryInstructions(aux);
                            writer.write("\timul " + destAndArg1 + ", " + (arg2 != null ? arg2 : obj2));
                            writer.write(System.lineSeparator());*/

                            // TODO: take care of data width movsw -> extending to 32-bit

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
                            /*RegisterDescriptor zeroRegister = resourceManager.getRegisterByForce(aux);
                            Descriptor source = resourceManager.getRegister(obj1, aux);

                            issueAuxiliaryInstructions(aux);
                            writer.write("\txor " + zeroRegister + ", " + zeroRegister);
                            writer.write(System.lineSeparator());
                            zeroRegister.setPrintWidth(SystemV_ABI.getX64VariableSize(obj1.getType()));
                            writer.write("\tsub " + zeroRegister + ", " + (source != null ? source : obj2));
                            writer.write(System.lineSeparator());

                            resourceManager.invalidateFromRegister(zeroRegister, aux);
                            resourceManager.validate(objResult, zeroRegister, true);

                            break;*/
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
                            resourceManager.saveDirtyVariables(aux, false);
                            issueAuxiliaryInstructions(aux);

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
                            // TODO: save eax, ecx, edx -> check for dirty only
                            Descriptor destination = resourceManager.getAddressDescriptor(objResult);

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
                            // TODO: restore eax, ecx, edx -> check for dirty only

                            break;
                        }

                        case PRINTF: {
                            // TODO: save eax, ecx, edx -> check for dirty only
                            /*Descriptor source = resourceManager.getRegister(obj2, aux);

                            issueAuxiliaryInstructions(aux);

                            // print format -> equivalent with mov rdi, offset FORMAT
                            writer.write("\tlea rdi, [rip + " + (obj2.getType().getKind() == Struct.Int ? integerTypeLabel : nonIntegerTypeLabel) + "]");
                            writer.write(System.lineSeparator());
                            // obj
                            int sourceSize = SystemV_ABI.getX64VariableSize(source.getHoldsValueOf().getType());
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

                            writer.write("\tmov " + regName + ", " + source);
                            writer.write(System.lineSeparator());
                            // clear eax -> for variable number of vector registers
                            writer.write("\txor eax, eax");
                            writer.write(System.lineSeparator());
                            // invoke
                            writer.write("\tcall printf");
                            writer.write(System.lineSeparator());
                            // TODO: restore eax, ecx, edx -> check for dirty only*/

                            break;
                        }

                        //////////////////////////////////////////////////////////////////////////////////
                        // BRANCHES AND LABEL GENERATING
                        //////////////////////////////////////////////////////////////////////////////////

                        case JMP: {
                            writer.write("\tjmp " + quadruple.getResult());
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
                }

                // TODO: resourceManager.saveDirtyVariables(aux);
                // TODO: issueAuxiliaryInstructions(aux);
                // TODO: resourceManager.saveContext();
                // TODO: writer.write( machine code );
                // TODO: resourceManager.restoreContext();
            }
        }
    }

    private void giveAddressToTemps(BasicBlock basicBlock, int startValue) {
        Collection<Obj> tempVars = basicBlock.temporaryVariables;
        for (Obj obj : tempVars) {
            int objSize = SystemV_ABI.getX64VariableSize(obj.getType());
            obj.setAdr(startValue + objSize);
            startValue += objSize;
        }
    }

    private void issueAuxiliaryInstructions(List<String> aux) throws IOException {
        for (String line : aux) {
            writer.write(line);
            writer.write(System.lineSeparator());
        }
    }
}
