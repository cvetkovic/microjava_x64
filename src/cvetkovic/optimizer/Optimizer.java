package cvetkovic.optimizer;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Optimizer {

    private List<OptimizerPass> optimizationList = new ArrayList<>();
    protected List<CodeSequence> codeSequenceList = new ArrayList<>();

    protected Optimizer() {
    }

    protected void addOptimizationPass(OptimizerPass optimizerPass) {
        optimizationList.add(optimizerPass);
    }

    public void executeOptimizations() {
        for (OptimizerPass pass : optimizationList) {
            pass.optimize();
            pass.finalizePass();
        }

        reassembleBasicBlocks();
    }

    public List<List<Quadruple>> getOptimizationOutput() {
        List<List<Quadruple>> result = new ArrayList<>();

        for (int i = 0; i < codeSequenceList.size(); i++)
            result.add(codeSequenceList.get(i).code);

        return result;
    }

    private Map<Integer, BasicBlock> indexBasicBlockBeginnings(List<BasicBlock> blocks) {
        Map<Integer, BasicBlock> result = new HashMap<>();

        for (BasicBlock b : blocks)
            result.put(b.firstQuadruple, b);

        return result;
    }

    public List<List<Quadruple>> reassembleBasicBlocks() {
        List<List<Quadruple>> outputCode = new ArrayList<>();

        for (CodeSequence sequence : codeSequenceList) {
            List<Quadruple> methodCode = new ArrayList<>();
            Map<Integer, BasicBlock> leaders = indexBasicBlockBeginnings(sequence.basicBlocks);

            BasicBlock currentBlock = null;
            for (int index = 0; index < sequence.code.size(); ) {
                if (currentBlock == null)
                    currentBlock = leaders.get(index);

                methodCode.addAll(currentBlock.instructions);

                currentBlock.firstQuadruple = index;
                index = currentBlock.lastQuadruple + 1;
                currentBlock.lastQuadruple = currentBlock.firstQuadruple + currentBlock.instructions.size();
            }

            outputCode.add(methodCode);
            sequence.code = methodCode;
        }

        return outputCode;
    }

}
