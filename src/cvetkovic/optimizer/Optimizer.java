package cvetkovic.optimizer;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.ir.ssa.SSAConverter;

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
        for (CodeSequence sequence : codeSequenceList) {
            DominanceAnalyzer dominanceAnalyzer = new DominanceAnalyzer(sequence);
            SSAConverter ssaConverter = new SSAConverter(dominanceAnalyzer);

            // before SSA conversion
            DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\cfg_before_ssa.dot", dominanceAnalyzer.getBasicBlocks());
            DominanceAnalyzer.dumpDominatorTree("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\dominator_tree.dot", dominanceAnalyzer.getImmediateDominators());

            ssaConverter.doPhiPlacement();
            ssaConverter.renameVariables();

            DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\cfg_after_ssa_generation.dot", dominanceAnalyzer.getBasicBlocks());
        }

        /*for (OptimizerPass pass : optimizationList) {
            pass.optimize();
            pass.finalizePass();
        }*/

        //reassembleBasicBlocks();
    }

    public List<CodeSequence> getOptimizationOutput() {
        return codeSequenceList;
    }

    public List<List<Quadruple>> reassembleBasicBlocks() {
        /*List<List<Quadruple>> outputCode = new ArrayList<>();

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

                currentBlock = null;
            }

            outputCode.add(methodCode);
            sequence.code = methodCode;
        }*/

        throw new RuntimeException("Not yet implemented.");

        //return outputCode;
    }

}
