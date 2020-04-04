package cvetkovic.ir.optimizations;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.misc.Utility;
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
            for (BasicBlock b : sequence.basicBlocks)
                System.out.println(b);

            BasicBlock enterBlock = null;
            for (BasicBlock b : sequence.basicBlocks)
                if (b.isEntryBlock())
                    enterBlock = b;
            if (enterBlock == null)
                throw new RuntimeException("Invalid code sequence for loop discovery as entry block has not been found.");

            /*sequence.loops = BasicBlock.discoverCycles(enterBlock);

            System.out.println("");
            System.out.println("Cycles detected in function '" + quadrupleList.get(0).getArg1() + "':");
            System.out.println(Utility.printCycle(sequence.loops));*/

            // TODO: remove this
            BasicBlock.experimentalCycleDiscovery(enterBlock);

            sequence.loops = BasicBlock.discoverLoops(sequence.loops);

            System.out.println("Loops detected in function '" + quadrupleList.get(0).getArg1() + "':");
            System.out.println(Utility.printCycle(sequence.loops));
        }

        createOptimizationList();
    }

    private void createOptimizationList() {
        // TODO: instantiate classes that implement OptimizerPass and add them to list of passes
    }
}
