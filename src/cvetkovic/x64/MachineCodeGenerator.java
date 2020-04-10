package cvetkovic.x64;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.QuadrupleIntegerConst;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MachineCodeGenerator {

    private String outputFileUrl;
    private File outputFileHandle;
    private BufferedWriter writer;

    private List<List<Quadruple>> instructions;
    private List<Obj> globalVariables;

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

    private void generateDirectives() throws IOException {
        writer.write(".global main");
        writer.write(System.lineSeparator());
        writer.write(System.lineSeparator());
    }

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

    private void generateFunctionsBody() throws IOException {
        writer.write(".section .text");
        writer.write(System.lineSeparator());

        for (List<Quadruple> sequence : instructions) {
            for (Quadruple quadruple : sequence) {

                switch (quadruple.getInstruction()) {
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
            }

            writer.write(System.lineSeparator());

            //writer.write("----------------------------------------------------------------------------");
            //writer.write(System.lineSeparator());
        }
    }

    // TODO: close writer somewhere
    // TODO: decide between Intel or AT&T syntax

    public void generateCode() {
        try {
            generateDirectives();
            generateBSS();
            generateFunctionsBody();

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
