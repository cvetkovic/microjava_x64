package cvetkovic.ir.optimizations;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.optimizer.Optimizer;

import java.util.List;
import java.util.Map;

public class IROptimizer extends Optimizer {

    private List<BasicBlock> basicBlocks;
    private Map<String, Integer> labelIndices;

    public IROptimizer(List<Quadruple> code) {
        super(code);

        labelIndices = BasicBlock.generateMapOfLabels(code);
        basicBlocks = BasicBlock.extractBasicBlocksFromSequence(code, labelIndices);

        for (BasicBlock b : basicBlocks)
            System.out.println(b);

        createOptimizationList();
    }

    private void createOptimizationList()
    {
        // TODO: instantiate classes thas implement OptimizerPass and add them to list of passes
    }
}
