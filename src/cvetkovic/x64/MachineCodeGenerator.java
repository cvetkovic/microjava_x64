package cvetkovic.x64;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.optimizations.local.AlgebraicIdentities;
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
import java.util.List;
import java.util.Set;

public class MachineCodeGenerator {

    private static final String[] registerNames = {"rax", "rbx", "rcx", "rdx", "rdi", "rsi"};

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
        for (String id : registerNames)
            registers.add(new RegisterDescriptor(id));

        List<BasicBlock.Tuple<Obj, Boolean>> memoryLocationList = new ArrayList<>();
        for (Obj var : basicBlock.nonTemporaryVariables)
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

                    switch (quadruple.getInstruction()) {
                        case ADD: {
                            if (AlgebraicIdentities.isIncInstructionArgs(obj1, obj2)) {
                                /*Obj var = obj1.getKind() != Obj.Con ? obj1 : obj2;
                                Descriptor arg = resourceManager.getRegister(var, aux);

                                writer.write("\tinc " + arg);
                                resourceManager.invalidateFromRegister(arg);*/
                            }
                            else {
                                boolean operandsSwapped = false;
                                Descriptor destAndArg1 = resourceManager.getRegister(obj1, aux);
                                if (destAndArg1 == null) {
                                    destAndArg1 = resourceManager.getRegister(obj2, aux);
                                    operandsSwapped = true;
                                }
                                Descriptor arg2 = (!operandsSwapped ? resourceManager.getRegister(obj2, aux, true) : resourceManager.getRegister(obj1, aux, true));

                                resourceManager.invalidateFromRegister(destAndArg1, aux);
                                resourceManager.validate(objResult, destAndArg1, true);

                                issueAuxiliaryInstructions(aux);
                                writer.write("\tadd " + destAndArg1 + ", " + arg2);
                                writer.write(System.lineSeparator());
                            }

                            break;
                        }
                        case SUB: {


                            break;
                        }
                        case STORE: {
                            Descriptor source = resourceManager.getRegister(obj1, aux);
                            //Descriptor destination = resourceManager.getRegister(objResult, aux);

                            issueAuxiliaryInstructions(aux);

                            resourceManager.validate(objResult, source, true);
                            //resourceManager.invalidate();

                            break;
                        }
                        case ENTER: {
                            writer.write("\tpush rbp");
                            writer.write(System.lineSeparator());
                            writer.write("\tmov rbp, rsp");
                            writer.write(System.lineSeparator());

                            QuadrupleIntegerConst allocateSize = (QuadrupleIntegerConst) quadruple.getArg1();
                            if (allocateSize.getValue() > 0) {
                                // has to be divisible by 16 by System V ABI
                                writer.write("\tsub rsp, " + SystemV_ABI.alignTo16(allocateSize.getValue()));
                                writer.write(System.lineSeparator());
                            }

                            break;
                        }
                        case LEAVE: {
                            writer.write("\tleave");
                            writer.write(System.lineSeparator());
                            writer.write("\tret");
                            writer.write(System.lineSeparator());

                            break;
                        }

                        //////////////////////////////////////////////////////////////////////////////////
                        // CONSTRUCTOR & STATIC INITIALIZATION
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
                            Descriptor source = resourceManager.getRegister(obj2, aux);

                            issueAuxiliaryInstructions(aux);

                            // print format -> equivalent with mov rdi, offset FORMAT
                            writer.write("\tlea rdi, [rip + " + (obj2.getType().getKind() == Struct.Int ? integerTypeLabel : nonIntegerTypeLabel) + "]");
                            writer.write(System.lineSeparator());
                            // obj
                            writer.write("\tmov rsi, " + source);
                            writer.write(System.lineSeparator());
                            // clear eax -> for variable number of vector registers
                            writer.write("\txor eax, eax");
                            writer.write(System.lineSeparator());
                            // invoke
                            writer.write("\tcall printf");
                            writer.write(System.lineSeparator());
                            // TODO: restore eax, ecx, edx -> check for dirty only

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

    private void issueAuxiliaryInstructions(List<String> aux) throws IOException {
        for (String line : aux) {
            writer.write(line);
            writer.write(System.lineSeparator());
        }
    }
}
