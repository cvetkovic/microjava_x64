package cvetkovic.ir.optimizations;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.optimizer.Optimizer;

import java.util.List;

public class IROptimizer extends Optimizer {

    public IROptimizer(List<List<Quadruple>> code) {
        for (List<Quadruple> quadrupleList : code) {
            CodeSequence sequence = new CodeSequence();

            sequence.code = quadrupleList;
            sequence.labelIndices = BasicBlock.generateMapOfLabels(quadrupleList);
            sequence.basicBlocks = BasicBlock.extractBasicBlocksFromSequence(quadrupleList, sequence.labelIndices);

            System.out.println("Basic blocks in function '" + quadrupleList.get(0).getArg1() + "':");
            for (BasicBlock b : sequence.basicBlocks) {
                System.out.println(b);
                System.out.println(b.printBasicBlock(sequence.code));
            }

            BasicBlock enterBlock = null;
            for (BasicBlock b : sequence.basicBlocks)
                if (b.isEntryBlock())
                    enterBlock = b;
            if (enterBlock == null)
                throw new RuntimeException("Invalid code sequence for loop discovery as entry block has not been found.");

            BasicBlock.determineNextUse(sequence);

            /*sequence.loops = BasicBlock.discoverCycles(enterBlock);

            System.out.println("");
            System.out.println("Cycles detected in function '" + quadrupleList.get(0).getArg1() + "':");
            System.out.println(Utility.printCycle(sequence.loops));

            sequence.loops = BasicBlock.discoverLoops(sequence.loops);

            System.out.println("Loops detected in function '" + quadrupleList.get(0).getArg1() + "':");
            System.out.println(Utility.printCycle(sequence.loops));*/

            codeSequenceList.add(sequence);
        }

        createOptimizationList();
    }

    private void createOptimizationList() {
        // TODO: instantiate classes that implement OptimizerPass and add them to list of passes
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < codeSequenceList.size(); i++) {
            stringBuilder.append("---------------------------------------------------------------------\n");

            CodeSequence sequence = codeSequenceList.get(i);
            /*String[] toWrite = new String[sequence.code.size() + sequence.basicBlocks.size()];

            for (int j = 0; j < sequence.code.size(); j++) {
                String instruction = sequence.code.get(j).toString();
                toWrite[j] = (instruction + "\n");
            }

            // insert newline after every basic block
            for (int j = 0; j < sequence.basicBlocks.size(); j++) {
                int writeNewLineAt = sequence.basicBlocks.get(j).basicBlockEnd + 1 + j;

                for (int k = toWrite.length - 1; k > writeNewLineAt; k--)
                    toWrite[k] = toWrite[k - 1];

                toWrite[writeNewLineAt] =  System.lineSeparator();
            }*/

            for(Quadruple q : sequence.code)
                stringBuilder.append(q + "\n");

            stringBuilder.append("---------------------------------------------------------------------\n");
        }

        return stringBuilder.toString();
    }
}
