package cvetkovic.optimizer;

import cvetkovic.ir.quadruple.Quadruple;

import java.util.ArrayList;
import java.util.List;

public abstract class Optimizer {

    private List<OptimizerPass> optimizationList = new ArrayList<>();
    private List<Quadruple> code;

    public Optimizer(List<Quadruple> code) {
        this.code = code;
    }

    protected void addOptimizationPass(OptimizerPass optimizerPass) {
        optimizationList.add(optimizerPass);
    }

    public void executeOptimizations() {
        for (OptimizerPass pass : optimizationList)
            pass.doOptimization();
    }

    public List<Quadruple> getOptimizationOutput() {
        return code;
    }
}
