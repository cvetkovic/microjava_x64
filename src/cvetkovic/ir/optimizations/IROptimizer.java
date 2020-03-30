package cvetkovic.ir.optimizations;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.optimizer.Optimizer;

import java.util.List;

public class IROptimizer extends Optimizer {

    public IROptimizer(List<Quadruple> code) {
        super(code);

        createOptimizationList();
    }

    private void createOptimizationList()
    {
        // TODO: instantiate classes thas implement OptimizerPass and add them to list of passes
    }
}
