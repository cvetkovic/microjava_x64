package cvetkovic.x64;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleIntegerConst;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.x64.cpu.RegisterDescriptor;
import cvetkovic.x64.cpu.ResourceManager;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MachineCodeGenerator {

    private static final String[] registerNames = { "RAX", "RBX", "RCX", "RDX", "RDI", "RSI" };

    private String outputFileUrl;
    private File outputFileHandle;
    private BufferedWriter writer;

    private List<List<Quadruple>> instructions;
    private List<Obj> globalVariables;
    private ResourceManager resourceManager;

    public MachineCodeGenerator(String outputFileUrl, List<List<Quadruple>> instructions, List<Obj> globalVariables) {
        this.outputFileUrl = outputFileUrl;
        this.instructions = instructions;
        this.globalVariables = globalVariables;

        try {
            outputFileHandle = new File(outputFileUrl);
            outputFileHandle.createNewFile();

            writer = new BufferedWriter(new FileWriter(outputFileHandle));
        } catch (Exception ex) {
            throw new RuntimeException("Cannot create output file.");
        }
    }

    /**
     * Initializes register and memory descriptors
     */
    private void initializeISATables() {
        List<RegisterDescriptor> registers = new ArrayList<>();
        for (String id : registerNames)
            registers.add(new RegisterDescriptor(id));

        resourceManager = new ResourceManager(registers);
    }

    /**
     * Generates Intel syntax directive and exports 'main' symbol
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
     * @throws IOException
     */
    private void generateBSS() throws IOException {
        writer.write(".section .bss");
        writer.write(System.lineSeparator());

        for (Obj var : globalVariables) {
            writer.write(var.getName() + ":");
            writer.write(System.lineSeparator());
            writer.write("\t" + DataStructures.getAssemblyDirectiveForAllocation(DataStructures.getX64VariableSize(var.getType())) + " 0x0");
            writer.write(System.lineSeparator());
        }

        writer.write(System.lineSeparator());
    }

    /**
     * Generates assembly code for methods' body
     * @throws IOException
     */
    private void generateFunctionsBody() throws IOException {
        writer.write(".section .text");
        writer.write(System.lineSeparator());

        for (List<Quadruple> sequence : instructions) {
            List<String> aux = new ArrayList<>();

            for (Quadruple quadruple : sequence) {
                Obj obj1 = (quadruple.getArg1() instanceof QuadrupleObjVar ? ((QuadrupleObjVar)quadruple.getArg1()).getObj() : null);
                Obj obj2 = (quadruple.getArg2() instanceof QuadrupleObjVar ? ((QuadrupleObjVar)quadruple.getArg2()).getObj() : null);
                Obj objResult = (quadruple.getResult() instanceof QuadrupleObjVar ? ((QuadrupleObjVar)quadruple.getResult()).getObj() : null);

                switch (quadruple.getInstruction()) {
                    case ADD:
                        RegisterDescriptor reg1 = resourceManager.getRegister(obj1, aux);
                        RegisterDescriptor reg2 = resourceManager.getRegister(obj2, aux);
                        RegisterDescriptor regResult = resourceManager.getRegister(objResult, aux);

                        issueAuxiliaryInstructions(aux);

                        writer.write("ADD " + regResult);

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

            if (instructions.get(instructions.size() - 1) != sequence) {
                writer.write(System.lineSeparator());
                //writer.write("----------------------------------------------------------------------------");
            }
        }
    }

    private void issueAuxiliaryInstructions(List<String> aux) throws IOException {
        for (String line : aux) {
            writer.write(line);
            writer.write(System.lineSeparator());
        }
    }

    /**
     * Generates assembly code for MicroJava program
     */
    public void generateCode() {
        try {
            initializeISATables();

            generateDirectives();
            generateBSS();
            generateFunctionsBody();

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
