package cvetkovic.x64;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.*;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.semantics.ClassMetadata;
import cvetkovic.structures.SymbolTable;
import cvetkovic.x64.cpu.Descriptor;
import cvetkovic.x64.cpu.RegisterDescriptor;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AssemblyGenerator {
    private static final String[][] registerNames = {
            new String[]{"rax", "eax", "al"},
            new String[]{"rbx", "ebx", "bl"},
            new String[]{"rcx", "ecx", "cl"},
            new String[]{"rdx", "edx", "dl"},
            new String[]{"rdi", "edi", "dil"},
            new String[]{"rsi", "esi", "sil"},
            new String[]{"r8", "r8d", "r8b"},
            new String[]{"r9", "r9d", "r9b"}};
    // TODO: add more registers (r10-r15)

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
    public AssemblyGenerator(String outputFileUrl, List<CodeSequence> codeSequences,
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

            // TODO: generate code for embedded functions (e.g. ord() and others)

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
    private void createRegisters(BasicBlock basicBlock) {
        registers.clear();
        mapToRegister.clear();

        for (String[] id : registerNames) {
            RegisterDescriptor newRegister = new RegisterDescriptor(id[2], id[1], id[0]);

            registers.add(newRegister);
            for (int i = 0; i < 3; i++)
                mapToRegister.put(id[i], newRegister);
        }

        resourceManager = new ResourceManager(registers);
    }

    private void createAddressDescriptors(BasicBlock basicBlock) {
        List<BasicBlock.Tuple<Obj, Boolean>> memoryLocationList = new ArrayList<>();
        for (Obj var : basicBlock.allVariables)
            if (var.parameter == false)
                memoryLocationList.add(new BasicBlock.Tuple<>(var, globalVariables.contains(var)));
        for (Obj var : basicBlock.enclosingFunction.getLocalSymbols()) {
            if (var.parameter) {
                memoryLocationList.add(new BasicBlock.Tuple<>(var, false));
            }
        }

        resourceManager.configureAddressDescriptors(memoryLocationList);
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
                table.append(System.lineSeparator());

                table.append("_vft_" + metadata.classObj.getName()).append(":").append(System.lineSeparator());

                List<Obj> methods = metadata.pointersToFunction.values().stream().filter(p -> p.getKind() == Obj.Meth || p.getKind() == SymbolTable.AbstractMethodObject).sorted(Comparator.comparingInt(Obj::getAdr)).collect(Collectors.toList());
                methods.forEach((n) -> table.append("\t.quad ").append(n.getName() + "_" + n.uniqueID).append("\n"));
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

        for (CodeSequence codeSequence : codeSequences) {
            BasicBlock basicBlock = null;

            for (int i = 0; i < codeSequence.basicBlocks.size(); i++) {
                if (basicBlock == null) {
                    // assign new basic block to compile
                    if (instructionCounter == 0)
                        basicBlock = codeSequence.entryBlock;
                    else
                        basicBlock = codeSequence.basicBlocks.stream().filter(p -> checkEquality(p.firstQuadruple, instructionCounter)).collect(Collectors.toList()).get(0);
                }

                List<String> aux = new ArrayList<>();
                boolean cancelSaveDirtyVals = false;

                // old register/memory descriptors are discarded
                createRegisters(basicBlock);

                SystemV_ABI_Call functionCall = new SystemV_ABI_Call(resourceManager);
                Stack<Obj> stackObjParameters = new Stack<>();
                Obj currentFunction = codeSequence.function;
                int paramIndex = 0;

                functionCall.generateCallForParameters(currentFunction);
                createAddressDescriptors(basicBlock);
                resourceManager.putParametersToRegisters(currentFunction);

                for (Quadruple quadruple : basicBlock.instructions) {
                    Obj obj1 = (quadruple.getArg1() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) quadruple.getArg1()).getObj() : null);
                    Obj obj2 = (quadruple.getArg2() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) quadruple.getArg2()).getObj() : null);
                    Obj objResult = (quadruple.getResult() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) quadruple.getResult()).getObj() : null);

                    int numOfArgsInMemory = 0;

                    switch (quadruple.getInstruction()) {
                        //////////////////////////////////////////////////////////////////////////////////
                        // ARITHMETIC OPERATORS (allowed only on int type in MikroJava - 32-bit)
                        //////////////////////////////////////////////////////////////////////////////////

                        case ADD:
                        case SUB:
                        case MUL: {
                            RegisterDescriptor dest_arg1_register = resourceManager.getRegister(obj1, quadruple);
                            RegisterDescriptor arg2_register = resourceManager.getRegister(obj2, quadruple, Collections.singletonList(dest_arg1_register));

                            resourceManager.fetchOperand(dest_arg1_register, obj1, aux);
                            resourceManager.fetchOperand(arg2_register, obj2, aux);

                            resourceManager.validate(dest_arg1_register, objResult, aux, true);
                            //resourceManager.makeDescriptorFree(arg2_register);

                            issueAuxiliaryInstructions(aux);
                            String secondOperand = arg2_register.getNameBySize(SystemV_ABI.getX64VariableSize(obj2.getType()));

                            String mnemonic;
                            if (quadruple.getInstruction() == IRInstruction.ADD)
                                mnemonic = "ADD";
                            else if (quadruple.getInstruction() == IRInstruction.SUB)
                                mnemonic = "SUB";
                            else if (quadruple.getInstruction() == IRInstruction.MUL)
                                mnemonic = "IMUL";
                            else
                                throw new RuntimeException("Not supported instruction type.");

                            writer.write("\t" + mnemonic + " " + dest_arg1_register + ", " + secondOperand);
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case DIV:
                        case REM: {
                            // EDX:EAX  -> number to divide
                            // CDQ      ->
                            // IDIV     -> divisor
                            // result:  EAX -> quotient
                            //          EDX -> remainder
                            RegisterDescriptor rax = resourceManager.getRegisterByName("rax");
                            RegisterDescriptor rdx = resourceManager.getRegisterByName("rdx");
                            List<RegisterDescriptor> forbidden = new ArrayList<>();
                            forbidden.add(rax);
                            forbidden.add(rdx);
                            RegisterDescriptor divisor = resourceManager.getRegister(obj2, quadruple, forbidden);
                            forbidden.add(divisor);
                            RegisterDescriptor result = resourceManager.getRegister(objResult, quadruple, forbidden);

                            resourceManager.forceTransferToMemory(rdx, aux);
                            resourceManager.fetchOperand(rax, obj1, aux);
                            issueAuxiliaryInstructions(aux);
                            aux.clear();

                            resourceManager.fetchOperand(divisor, obj2, aux);
                            resourceManager.validate(result, objResult, aux, true);

                            writer.write("\tCDQ");
                            writer.write(System.lineSeparator());

                            issueAuxiliaryInstructions(aux);

                            writer.write("\tIDIV " + divisor.getNameBySize(4));
                            writer.write(System.lineSeparator());

                            RegisterDescriptor resultRegSelection;
                            if (quadruple.getInstruction() == IRInstruction.DIV)
                                resultRegSelection = rax;
                            else if (quadruple.getInstruction() == IRInstruction.REM)
                                resultRegSelection = rdx;
                            else
                                throw new RuntimeException("Not supported instruction type.");

                            writer.write("\tMOV " + result + ", " + resultRegSelection.getNameBySize(4));
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case NEG: {
                            RegisterDescriptor zeroRegister = resourceManager.getRegister(null, quadruple);
                            RegisterDescriptor source = resourceManager.getRegister(obj1, quadruple, Collections.singletonList(zeroRegister));

                            resourceManager.fetchOperand(source, obj1, aux);
                            resourceManager.validate(zeroRegister, objResult, aux, true);
                            issueAuxiliaryInstructions(aux);

                            writer.write("\tXOR " + zeroRegister + ", " + zeroRegister);
                            writer.write(System.lineSeparator());
                            writer.write("\tSUB " + zeroRegister + ", " + source.getNameBySize(4));
                            writer.write(System.lineSeparator());

                            break;
                        }

                        //////////////////////////////////////////////////////////////////////////////////
                        // INSTRUCTIONS FOR MEMORY
                        //////////////////////////////////////////////////////////////////////////////////

                        case LOAD: {
                            RegisterDescriptor source_dest = resourceManager.getRegister(obj1, quadruple);

                            resourceManager.fetchOperand(source_dest, obj1, aux);
                            resourceManager.validate(source_dest, objResult, aux, true);

                            Struct destStruct = obj1.getType().getElemType();
                            String ptrSpecifier = SystemV_ABI.getPtrSpecifier(destStruct);
                            int destSize = SystemV_ABI.getX64VariableSize(destStruct);

                            writer.write("\tMOV " + source_dest.getNameBySize(destSize) + ", " + ptrSpecifier + " [" + source_dest.getNameBySize(8) + "]");
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case STORE: {
                            RegisterDescriptor arg1_result_register = resourceManager.getRegister(obj1, quadruple);

                            if (quadruple.getArg2() instanceof QuadruplePTR) {
                                // PTR -> load value that will be written
                                resourceManager.fetchOperand(arg1_result_register, obj1, aux);

                                RegisterDescriptor pointerToDestination = resourceManager.getRegister(objResult, quadruple, Collections.singletonList(arg1_result_register));
                                resourceManager.fetchOperand(pointerToDestination, objResult, aux);

                                issueAuxiliaryInstructions(aux);

                                Struct targetStruct = objResult.getType().getElemType();
                                String pointerTargetSize = SystemV_ABI.getPtrSpecifier(targetStruct);
                                int dataSize = SystemV_ABI.getX64VariableSize(targetStruct);

                                writer.write("\tMOV " + pointerTargetSize + " [" + pointerToDestination.getNameBySize(8) + "], " + arg1_result_register.getNameBySize(dataSize));
                                writer.write(System.lineSeparator());
                            }
                            else {
                                // NON PTR -> load value that will be written
                                resourceManager.fetchOperand(arg1_result_register, obj1, aux);
                                resourceManager.validate(arg1_result_register, objResult, aux, true);
                                issueAuxiliaryInstructions(aux);
                            }

                            break;
                        }

                        case MALLOC: {
                            Struct elemType;
                            if (objResult.getType().getKind() == Struct.Array)
                                elemType = objResult.getType().getElemType();
                            else
                                elemType = objResult.getType();

                            List<String> tmp = new ArrayList<>();
                            resourceManager.saveDirtyVariables(aux, true);
                            issueAuxiliaryInstructions(aux);

                            int numberOfElements;
                            if (quadruple.getArg1() instanceof QuadrupleObjVar) {
                                // instantiating new class
                                Struct type = obj1.getType();
                                int classSize = 0;

                                for (Obj member : type.getMembers()) {
                                    if (member.getName().equals("extends"))
                                        continue;
                                    else if (member.getKind() == Obj.Meth)
                                        continue;

                                    if (member.getName().equals("_vtp"))
                                        classSize += SystemV_ABI.getX64VariableSize(new Struct(Struct.Class));
                                    else
                                        classSize += SystemV_ABI.getX64VariableSize(member.getType());
                                }

                                numberOfElements = classSize;
                            }
                            else
                                numberOfElements = ((QuadrupleIntegerConst) quadruple.getArg1()).getValue();

                            resourceManager.invalidateAddressDescriptors("rdi");
                            resourceManager.invalidateAddressDescriptors("rsi");
                            resourceManager.invalidateAddressDescriptors("rax");

                            // num of elements
                            writer.write("\tMOV rdi, " + String.valueOf(numberOfElements));
                            writer.write(System.lineSeparator());
                            // single element size in bytes
                            int allocationSize;
                            if (objResult.getType().getKind() == Struct.Class)
                                allocationSize = 1;
                            else
                                allocationSize = SystemV_ABI.getX64VariableSize(elemType);
                            writer.write("\tMOV rsi, " + allocationSize);
                            writer.write(System.lineSeparator());
                            // clear eax -> for variable number of vector registers
                            writer.write("\tXOR eax, eax");
                            writer.write(System.lineSeparator());
                            // invoke calloc
                            writer.write("\tCALL calloc");
                            writer.write(System.lineSeparator());

                            if (objResult.getType().getKind() == Struct.Class) {
                                writer.write("\tMOV QWORD PTR [rax], OFFSET _vft_" + obj1.getName());
                                writer.write(System.lineSeparator());
                            }

                            if (quadruple.getArg2() == null || quadruple.getArg2() instanceof QuadrupleARR) {
                                // allocates array -> save pointer to objResult's address
                                writer.write("\tMOV " + resourceManager.getMemoryDescriptor(objResult) + ", rax");
                                writer.write(System.lineSeparator());
                            }
                            else {
                                // MALLOC as PTR
                                RegisterDescriptor a_reg = mapToRegister.get(registerNames[0][0]);
                                RegisterDescriptor destination = resourceManager.getRegister(objResult, quadruple, Collections.singletonList(a_reg));

                                resourceManager.fetchOperand(destination, objResult, aux);
                                resourceManager.validate(destination, objResult, aux, true);

                                // TODO: test this type of allocation
                                issueAuxiliaryInstructions(aux);
                                // save PTR
                                writer.write("\tMOV [" + destination + "], rax");
                                writer.write(System.lineSeparator());
                            }

                            writer.write(System.lineSeparator());

                            resourceManager.invalidateRegisters();

                            break;
                        }

                        case ALOAD: {
                            Obj arrayReference = obj1;
                            Obj arrayIndex = obj2;
                            Obj destinationVariable = objResult;

                            Struct elemTypeStruct = arrayReference.getType().getElemType();
                            int elemTypeSizeInByte = SystemV_ABI.getX64VariableSize(elemTypeStruct);

                            List<RegisterDescriptor> forbidden = new ArrayList<>();
                            RegisterDescriptor regArrayReference = resourceManager.getRegister(arrayReference, quadruple);
                            forbidden.add(regArrayReference);
                            RegisterDescriptor regArrayIndex = resourceManager.getRegister(arrayIndex, quadruple, forbidden);
                            forbidden.add(regArrayIndex);
                            RegisterDescriptor regDestination = resourceManager.getRegister(destinationVariable, quadruple, forbidden);

                            resourceManager.fetchOperand(regArrayReference, arrayReference, aux);
                            resourceManager.setSXD();
                            resourceManager.fetchOperand(regArrayIndex, arrayIndex, aux);
                            resourceManager.validate(regDestination, destinationVariable, aux, true);

                            issueAuxiliaryInstructions(aux);
                            String saveInstruction = "[" + regArrayReference.getNameBySize(8) + " + " + elemTypeSizeInByte + " * " + regArrayIndex.getNameBySize(8) + "]";

                            writer.write("\tMOV " + regDestination.getNameBySize(elemTypeSizeInByte) + ", " + SystemV_ABI.getPtrSpecifier(elemTypeStruct) + " " + saveInstruction);
                            writer.write(System.lineSeparator());


                            break;
                        }

                        case ASTORE: {
                            Obj arrayReference = objResult;
                            Obj arrayIndex = obj2;
                            Obj valueToWrite = obj1;

                            Struct elemTypeStruct = arrayReference.getType().getElemType();
                            int elemTypeSizeInByte = SystemV_ABI.getX64VariableSize(elemTypeStruct);

                            List<RegisterDescriptor> forbidden = new ArrayList<>();
                            RegisterDescriptor regArrayReference = resourceManager.getRegister(arrayReference, quadruple);
                            forbidden.add(regArrayReference);
                            RegisterDescriptor regArrayIndex = resourceManager.getRegister(arrayIndex, quadruple, forbidden);
                            forbidden.add(regArrayIndex);
                            RegisterDescriptor regValue = resourceManager.getRegister(valueToWrite, quadruple, forbidden);

                            resourceManager.fetchOperand(regArrayReference, arrayReference, aux);
                            resourceManager.setSXD();
                            resourceManager.fetchOperand(regArrayIndex, arrayIndex, aux);
                            resourceManager.fetchOperand(regValue, valueToWrite, aux);

                            issueAuxiliaryInstructions(aux);
                            String saveInstruction = "[" + regArrayReference.getNameBySize(8) + " + " + elemTypeSizeInByte + " * " + regArrayIndex.getNameBySize(8) + "]";

                            writer.write("\tMOV " + SystemV_ABI.getPtrSpecifier(elemTypeStruct) + " " + saveInstruction + ", " + regValue.getNameBySize(elemTypeSizeInByte));
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case GET_PTR: {
                            RegisterDescriptor basePointer = resourceManager.getRegister(obj1, quadruple);
                            int fieldOffset = obj2.getAdr();

                            resourceManager.fetchOperand(basePointer, obj1, aux);
                            resourceManager.validate(basePointer, objResult, aux, true);

                            issueAuxiliaryInstructions(aux);

                            writer.write("\tADD " + basePointer + ", " + fieldOffset);
                            writer.write(System.lineSeparator());

                            break;
                        }

                        //////////////////////////////////////////////////////////////////////////////////
                        // FUNCTION CALLS & STACK FRAME OPERATIONS
                        //////////////////////////////////////////////////////////////////////////////////

                        case PARAM: {
                            RegisterDescriptor paramRegister = functionCall.getParameterRegister(paramIndex++);

                            if (paramRegister != null) {
                                resourceManager.fetchOperand(paramRegister, obj1, aux);
                                issueAuxiliaryInstructions(aux);
                            }
                            else {
                                stackObjParameters.push(obj1);
                            }

                            break;
                        }

                        case CALL:
                        case INVOKE_VIRTUAL: {
                            Obj methodToInvoke = obj1;

                            // passing parameters through register
                            List<String> params = functionCall.generateCallForParameters(methodToInvoke);

                            // passing parameters through stack
                            while (!stackObjParameters.empty()) {
                                Obj param = stackObjParameters.pop();

                                List<RegisterDescriptor> forbiddenList = getParamStackCallForbiddenList(functionCall);
                                RegisterDescriptor argToPush = resourceManager.getRegister(param, quadruple, forbiddenList);
                                resourceManager.fetchOperand(argToPush, param, params);

                                params.add("\tPUSHQ " + argToPush.getNameBySize(8));

                                resourceManager.clearRegisterFromAddressDescriptors(param);
                                argToPush.setHoldsValueOf(null);
                            }

                            // emitting instructions for parameters
                            issueAuxiliaryInstructions(params);
                            params.clear();

                            resourceManager.saveDirtyVariables(aux, true);
                            issueAuxiliaryInstructions(aux);

                            if (quadruple.getInstruction() == IRInstruction.CALL) {
                                writer.write("\tCALL " + methodToInvoke + "_" + methodToInvoke.uniqueID);
                                writer.write(System.lineSeparator());

                                resourceManager.clearRegisterFromAddressDescriptors(methodToInvoke);
                            }
                            else if (quadruple.getInstruction() == IRInstruction.INVOKE_VIRTUAL) {
                                List<RegisterDescriptor> forbiddenList = getParamStackCallForbiddenList(functionCall);

                                RegisterDescriptor edi = resourceManager.getRegisterByName("rdi");
                                Obj method = edi.getHoldsValueOf();

                                RegisterDescriptor ptrToClass = resourceManager.getRegister(method, quadruple, forbiddenList);
                                resourceManager.fetchOperand(ptrToClass, method, aux);

                                issueAuxiliaryInstructions(aux);
                                aux.clear();

                                writer.write("\tMOV " + ptrToClass + ", [" + ptrToClass + "]");
                                writer.write(System.lineSeparator());
                                writer.write("\tCALL [" + ptrToClass + " + 8 * " + methodToInvoke.getAdr() + "]");
                                writer.write(System.lineSeparator());

                                ptrToClass.setHoldsValueOf(null);
                                edi.setHoldsValueOf(null);
                                resourceManager.clearRegisterFromAddressDescriptors(method);
                            }
                            else
                                throw new RuntimeException("Not supported type of method call.");

                            long numberOfStackParameters = methodToInvoke.getLocalSymbols().stream().filter(p -> p.stackParameter).count();
                            if (numberOfStackParameters > 1) {
                                writer.write("\tADD rsp, " + numberOfStackParameters * 8);
                                writer.write(System.lineSeparator());
                            }

                            if (methodToInvoke.getType().getKind() != Struct.None) {
                                RegisterDescriptor rax = resourceManager.getRegisterByName("rax");
                                resourceManager.validate(rax, objResult, aux, true);
                                resourceManager.saveReturnedValueToMemory(rax, objResult, aux);
                            }

                            issueAuxiliaryInstructions(aux);

                            paramIndex = 0;

                            writer.write(System.lineSeparator());

                            break;
                        }

                        case ENTER: {
                            writer.write("\tPUSH rbp");
                            writer.write(System.lineSeparator());
                            writer.write("\tMOV rbp, rsp");
                            writer.write(System.lineSeparator());

                            QuadrupleIntegerConst allocateSize = (QuadrupleIntegerConst) quadruple.getArg1();

                            // has to be divisible by 16 by System V ABI
                            writer.write("\tSUB rsp, " + allocateSize.getValue());
                            writer.write(System.lineSeparator());

                            resourceManager.saveRegisterFile(aux);
                            issueAuxiliaryInstructions(aux);

                            break;
                        }

                        case LEAVE: {
                            // save dirty variables
                            resourceManager.saveDirtyVariables(aux, false);
                            issueAuxiliaryInstructions(aux);
                            aux.clear();
                            cancelSaveDirtyVals = true;

                            resourceManager.restoreRegisterFile(aux);
                            issueAuxiliaryInstructions(aux);

                            writer.write("\tLEAVE");
                            writer.write(System.lineSeparator());
                            writer.write("\tRET");
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case RETURN: {
                            RegisterDescriptor reg_a = resourceManager.getRegisterByName("rax");
                            RegisterDescriptor reg_source = resourceManager.getRegister(obj1, quadruple, Collections.singletonList(reg_a));

                            resourceManager.fetchOperand(reg_source, obj1, aux);
                            //resourceManager.validate(reg_a, obj1, aux, true);
                            // NOTE: no need to register as the next instruction shall be LEAVE
                            issueAuxiliaryInstructions(aux);

                            writer.write("\tMOV " + reg_a.getNameBySize(SystemV_ABI.getX64VariableSize(obj1.getType())) + ", " + reg_source);
                            writer.write(System.lineSeparator());

                            break;
                        }

                        //////////////////////////////////////////////////////////////////////////////////
                        // INPUT / OUTPUT
                        //////////////////////////////////////////////////////////////////////////////////

                        case SCANF: {
                            Descriptor destination = resourceManager.getMemoryDescriptor(objResult);

                            List<RegisterDescriptor> toPreserve = new ArrayList<>();
                            resourceManager.saveDirtyVariables(aux, true);
                            issueAuxiliaryInstructions(aux);

                            resourceManager.invalidateAddressDescriptors("rdi");
                            resourceManager.invalidateAddressDescriptors("rsi");
                            resourceManager.invalidateAddressDescriptors("rax");

                            // print format -> equivalent with mov rdi, offset FORMAT
                            writer.write("\tLEA rdi, [rip + " + (objResult.getType().getKind() == Struct.Int ? integerTypeLabel : nonIntegerTypeLabel) + "]");
                            writer.write(System.lineSeparator());
                            // obj
                            writer.write("\tLEA rsi, " + destination);
                            writer.write(System.lineSeparator());
                            // clear eax -> for variable number of vector registers
                            writer.write("\tXOR eax, eax");
                            writer.write(System.lineSeparator());
                            // invoke
                            writer.write("\tCALL scanf");
                            writer.write(System.lineSeparator());

                            resourceManager.invalidateRegisters();

                            break;
                        }

                        case PRINTF: {
                            RegisterDescriptor source = resourceManager.getRegister(obj2, quadruple);

                            List<String> tmp = new ArrayList<>();
                            resourceManager.fetchOperand(source, obj2, tmp);

                            // obj
                            int sourceSize = SystemV_ABI.getX64VariableSize(obj2.getType());

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
                            resourceManager.saveDirtyVariables(aux, true);
                            issueAuxiliaryInstructions(aux);

                            resourceManager.invalidateAddressDescriptors("rdi");
                            resourceManager.invalidateAddressDescriptors("rax");

                            // data to be printed
                            if (source.getHoldsValueOf() != null || obj2.getKind() == Obj.Con) { // otherwise operand will be fetched by fetchOperandInstruction
                                source.setPrintWidth(sourceSize);
                                writer.write("\tMOV " + regName + ", " + source);
                                writer.write(System.lineSeparator());
                            }
                            // print format -> equivalent with mov rdi, offset FORMAT
                            String formatterLabel;
                            switch (((QuadrupleIODataWidth)quadruple.getArg1()).getWidth())
                            {
                                case BIT:
                                case WORD:
                                    formatterLabel = integerTypeLabel;
                                    break;

                                case BYTE:
                                    formatterLabel = nonIntegerTypeLabel;
                                    break;

                                default:
                                    throw new RuntimeException("Not supported PRINTF formatter.");
                            }
                            writer.write("\tLEA rdi, [rip + " + formatterLabel + "]");
                            writer.write(System.lineSeparator());
                            // clear eax -> for variable number of vector registers
                            writer.write("\tXOR eax, eax");
                            writer.write(System.lineSeparator());
                            // invoke
                            writer.write("\tCALL printf");
                            writer.write(System.lineSeparator());

                            resourceManager.invalidateRegisters();

                            /*aux.clear();
                            resourceManager.restoreContext(toPreserve, aux);
                            issueAuxiliaryInstructions(aux);*/

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

                            writer.write("\tJMP " + quadruple.getResult());
                            writer.write(System.lineSeparator());

                            break;
                        }

                        case JL:
                        case JLE:
                        case JG:
                        case JGE:
                        case JE:
                        case JNE: {
                            RegisterDescriptor dest_arg1_register = resourceManager.getRegister(obj1, quadruple);
                            RegisterDescriptor arg2_register = resourceManager.getRegister(obj2, quadruple, Collections.singletonList(dest_arg1_register));

                            resourceManager.fetchOperand(dest_arg1_register, obj1, aux);
                            resourceManager.fetchOperand(arg2_register, obj2, aux);

                            issueAuxiliaryInstructions(aux);
                            String secondOperand = arg2_register.getNameBySize(SystemV_ABI.getX64VariableSize(obj2.getType()));

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
                            String labelName;

                            if (instructionCounter == 0 && !currentFunction.getName().equals("main"))
                                labelName = currentFunction.getName() + "_" + currentFunction.uniqueID;
                            else
                                labelName = quadruple.getArg1().toString();

                            writer.write(labelName + ":");
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
            }

            instructionCounter = 0;
        }
    }

    private List<RegisterDescriptor> getParamStackCallForbiddenList(SystemV_ABI_Call functionCall) {
        List<RegisterDescriptor> regs = new ArrayList<>();
        for (int i = 0; i < 6; i++)
            regs.add(functionCall.getParameterRegister(i));

        return regs;
    }

    private void makeRegisterPreservationList(List<RegisterDescriptor> toPreserve) {
        for (int i = 0; i < registerNames.length; i++)
            if (resourceManager.checkIfRegisterIsTaken(mapToRegister.get(registerNames[i][0])))
                toPreserve.add(mapToRegister.get(registerNames[i][0]));
    }

    private void issueAuxiliaryInstructions(List<String> aux) throws IOException {
        for (String line : aux) {
            writer.write(line);
            writer.write(System.lineSeparator());
        }
    }
}
