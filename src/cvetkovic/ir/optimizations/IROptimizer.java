package cvetkovic.ir.optimizations;

import cvetkovic.ir.optimizations.local.IdentityElimination;
import cvetkovic.ir.optimizations.local.LocalValueNumbering;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.Optimizer;

import java.util.List;

public class IROptimizer extends Optimizer {
    public IROptimizer(List<List<Quadruple>> code) {
        for (List<Quadruple> quadrupleList : code) {
            CodeSequence sequence = new CodeSequence();

            sequence.code = quadrupleList;
            sequence.labelIndices = BasicBlock.generateMapOfLabels(quadrupleList);
            sequence.basicBlocks = BasicBlock.extractBasicBlocksFromSequence(quadrupleList, sequence.labelIndices);

            for (BasicBlock b : sequence.basicBlocks)
                if (b.isEntryBlock())
                    sequence.entryBlock = b;
            if (sequence.entryBlock == null)
                throw new RuntimeException("Invalid code sequence for loop discovery as entry block has not been found.");

            codeSequenceList.add(sequence);
        }

        createOptimizationList();
    }

    private void createOptimizationList() {
        for (CodeSequence sequence : codeSequenceList) {
            // LOCAL OPTIMIZATIONS
            for (BasicBlock block : sequence.basicBlocks) {
                addOptimizationPass(new LocalValueNumbering(block));
                addOptimizationPass(new IdentityElimination(block));
                //addOptimizationPass(new DeadCodeElimination(block));
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < codeSequenceList.size(); i++) {
            stringBuilder.append("---------------------------------------------------------------------\n");

            CodeSequence sequence = codeSequenceList.get(i);
            for (Quadruple q : sequence.code)
                stringBuilder.append(q + "\n");

            stringBuilder.append("---------------------------------------------------------------------\n");
        }

        return stringBuilder.toString();
    }
}
