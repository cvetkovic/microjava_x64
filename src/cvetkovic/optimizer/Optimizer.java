package cvetkovic.optimizer;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.ir.ssa.SSAConverter;

import java.util.*;

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

            DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\cfg_ssa_before_optimizer.dot", dominanceAnalyzer.getBasicBlocks());

            /*for (OptimizerPass pass : optimizationList) {
                pass.optimize();
                pass.finalizePass();
            }*/

            // eliminating SSA from code
            ssaConverter.toNormalForm();

            DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\cfg_post_ssa.dot", dominanceAnalyzer.getBasicBlocks());
        }
    }

    public List<CodeSequence> getOptimizationOutput() {
        return codeSequenceList;
    }

    public List<BasicBlock> reassembleBasicBlocks() {
        List<BasicBlock> result = new ArrayList<>();

        Stack<BasicBlock> stack = new Stack<>();
        

        return result;
    }

}
