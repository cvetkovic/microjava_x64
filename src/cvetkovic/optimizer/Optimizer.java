package cvetkovic.optimizer;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Optimizer {

    private List<OptimizerPass> optimizationList = new ArrayList<>();
    protected List<CodeSequence> codeSequenceList = new ArrayList<>();

    protected Optimizer() {
    }

    protected void addOptimizationPass(OptimizerPass optimizerPass) {
        optimizationList.add(optimizerPass);
    }

    public void executeOptimizations() {
        for (OptimizerPass pass : optimizationList)
            pass.doOptimization();
    }

    public List<List<Quadruple>> getOptimizationOutput() {
        List<List<Quadruple>> result = new ArrayList<>();

        for (int i = 0; i < codeSequenceList.size(); i++)
            result.add(codeSequenceList.get(i).code);

        return result;
    }

    public static class CodeSequence {
        public List<Quadruple> code;
        public List<BasicBlock> basicBlocks;
        public Map<String, Integer> labelIndices;
        public List<Set<BasicBlock>> loops;
    }
}
