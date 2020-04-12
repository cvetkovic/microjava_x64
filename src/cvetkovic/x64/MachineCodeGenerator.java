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
            generateFunctionsBody();
            generatePolymorphismTables();

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
        writer.write(".intel_syntax");
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
                writer.write("\t" + ISADataWidthCalculator.getAssemblyDirectiveForAllocation(ISADataWidthCalculator.getX64VariableSize(var.getType())) + " 0x0");
                writer.write(System.lineSeparator());
            }

            writer.write(System.lineSeparator());
        }
    }

    private void generatePolymorphismTables() throws IOException {
        if (classMetadata.size() > 0) {
            writer.write(".section .data");
            writer.write(System.lineSeparator());

            StringBuilder table = new StringBuilder();

            for (ClassMetadata metadata : classMetadata) {
                table.append("; VFT for ").append(metadata.className).append("\n");
                metadata.pointersToFunction.forEach((n, p) -> table.append(n).append(":\n\t.quad ").append(p.getName()));
                table.append(System.lineSeparator());
            }

            writer.write(table.toString());
        }
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
                        case ADD:
                            if (AlgebraicIdentities.isIncInstructionArgs(obj1, obj2)) {
                            /*Obj var = obj1.getKind() != Obj.Con ? obj1 : obj2;
                            Descriptor arg = resourceManager.getRegister(var, aux, true);

                            writer.write("\tinc " + arg);
                            resourceManager.invalidate(var);*/
                                // TODO: do INC instruction
                            }
                            else {
                                boolean operandsSwapped = false;
                                RegisterDescriptor destAndArg1 = resourceManager.getRegister(obj1, aux);
                                if (destAndArg1 == null) {
                                    destAndArg1 = resourceManager.getRegister(obj2, aux);
                                    operandsSwapped = true;
                                }
                                Descriptor arg2 = (!operandsSwapped ? resourceManager.getRegister(obj2, aux, true) : resourceManager.getRegister(obj1, aux, true));

                                issueAuxiliaryInstructions(aux);
                                writer.write("\tadd " + destAndArg1 + ", " + arg2);
                                writer.write(System.lineSeparator());

                                resourceManager.validate(objResult, destAndArg1);
                                resourceManager.invalidate(!operandsSwapped ? obj1 : obj2);
                            }

                            break;

                        case SUB:


                            break;

                        case GEN_LABEL:
                            writer.write(quadruple.getArg1().toString() + ":");
                            writer.write(System.lineSeparator());
                            break;

                        case ENTER:
                            writer.write("\tpush rbp");
                            writer.write(System.lineSeparator());
                            writer.write("\tmov rbp, rsp");
                            writer.write(System.lineSeparator());

                            QuadrupleIntegerConst allocateSize = (QuadrupleIntegerConst) quadruple.getArg1();
                            if (allocateSize.getValue() > 0) {
                                // TODO: type in decimal or in HEX ?
                                writer.write("\tsub rsp, " + allocateSize.getValue());
                                writer.write(System.lineSeparator());
                            }

                            break;

                        case LEAVE:
                            writer.write("\tpop rbp");
                            writer.write(System.lineSeparator());
                            writer.write("\tret");
                            writer.write(System.lineSeparator());
                            break;

                        case JMP:
                            writer.write("\tjmp " + quadruple.getResult());
                            writer.write(System.lineSeparator());
                            break;

                        default:
                            break;
                        //throw new RuntimeException("Instruction not supported by x86-64 code generator.");
                    }

                    aux.clear();
                }

                /*if (instructions.get(instructions.size() - 1) != sequence) {
                    writer.write(System.lineSeparator());
                    //writer.write("----------------------------------------------------------------------------");
                }*/

                resourceManager.saveDirtyVariables(aux);
                issueAuxiliaryInstructions(aux);
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
