package cvetkovic.ir.optimizations;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.misc.Config;
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
            if (Config.printBasicBlockInfo || Config.printBasicBlockQuadruples) {
                for (BasicBlock b : sequence.basicBlocks) {
                    if (Config.printBasicBlockInfo)
                        System.out.println(b);

                    if (Config.printBasicBlockQuadruples)
                        System.out.println(b.printBasicBlock(sequence.code));
                }
            }

            BasicBlock enterBlock = null;
            for (BasicBlock b : sequence.basicBlocks)
                if (b.isEntryBlock())
                    enterBlock = b;
            if (enterBlock == null)
                throw new RuntimeException("Invalid code sequence for loop discovery as entry block has not been found.");

            BasicBlock.doLivenessAnalysis(sequence);

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
            for(Quadruple q : sequence.code)
                stringBuilder.append(q + "\n");

            stringBuilder.append("---------------------------------------------------------------------\n");
        }

        return stringBuilder.toString();
    }
}
