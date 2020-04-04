package cvetkovic.ir.optimizations;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.optimizer.Optimizer;

import java.util.List;
import java.util.Set;

public class IROptimizer extends Optimizer {

    public IROptimizer(List<List<Quadruple>> code) {
        for (List<Quadruple> quadrupleList : code) {
            CodeSequence sequence = new CodeSequence();

            sequence.code = quadrupleList;
            sequence.labelIndices = BasicBlock.generateMapOfLabels(quadrupleList);
            sequence.basicBlocks = BasicBlock.extractBasicBlocksFromSequence(quadrupleList, sequence.labelIndices);
            BasicBlock enterBlock = null;
            for (BasicBlock b : sequence.basicBlocks)
                if (b.isEntryBlock())
                    enterBlock = b;
            if (enterBlock == null)
                throw new RuntimeException("Invalid code sequence for loop discovery as entry block has not been found.");
            sequence.loops = BasicBlock.discoverLoops(enterBlock);

            System.out.println("Basic blocks in function '" + quadrupleList.get(0).getArg1() + "':");
            for (BasicBlock b : sequence.basicBlocks)
                System.out.println(b);
            System.out.println("");
            System.out.println("Loops detected in function '" + quadrupleList.get(0).getArg1() + "':");
            for (Set<BasicBlock> loop : sequence.loops) {
                StringBuilder s = new StringBuilder();
                s.append("(");
                loop.forEach(x -> s.append(x.blockId + ", "));
                s.append(")");

                System.out.println(s.toString().replace(", )", ")"));
            }
        }

        createOptimizationList();
    }

    private void createOptimizationList() {
        // TODO: instantiate classes that implement OptimizerPass and add them to list of passes
    }
}
