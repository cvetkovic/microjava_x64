package cvetkovic.x64;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.QuadrupleIntegerConst;

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

    public MachineCodeGenerator(String outputFileUrl, List<List<Quadruple>> instructions) {
        this.outputFileUrl = outputFileUrl;
        this.instructions = instructions;

        try {
            outputFileHandle = new File(outputFileUrl);
            outputFileHandle.createNewFile();

            writer = new BufferedWriter(new FileWriter(outputFileHandle));
        } catch (Exception ex) {
            throw new RuntimeException("Cannot create output file.");
        }
    }

    // TODO: close writer somewhere
    // TODO: decide between Intel or AT&T syntax

    public void generateCode() throws IOException {
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

        writer.close();
    }
}
