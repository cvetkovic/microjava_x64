package cvetkovic.optimizer;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.optimizations.ssa.DeadCodeElimination;
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
            DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\normal_cfg_pre_ssa_" + sequence.function + ".dot",
                    "C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\normal_rcfg_pre_ssa_" + sequence.function + ".dot",
                    dominanceAnalyzer.getBasicBlocks());
            DominanceAnalyzer.dumpDominatorTree("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\dominator_tree_" + sequence.function + ".dot",
                    dominanceAnalyzer.getImmediateDominators());
            DominanceAnalyzer.dumpDominatorTree("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\reverse_dominator_tree_" + sequence.function + ".dot",
                    dominanceAnalyzer.getReverseImmediateDominators());

            ssaConverter.doPhiPlacement();
            ssaConverter.renameVariables();

            DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\ssa_cfg_pre_opt_" + sequence.function + ".dot",
                    "C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\ssa_rcfg_pre_opt_" + sequence.function + ".dot",
                    dominanceAnalyzer.getBasicBlocks());

            //addOptimizationPass(new DeadCodeElimination(dominanceAnalyzer));

            for (OptimizerPass pass : optimizationList) {
                pass.optimize();
                pass.finalizePass();
            }

            DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\ssa_cfg_post_opt_" + sequence.function + ".dot",
                    "C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\ssa_rcfg_post_opt_" + sequence.function + ".dot",
                    dominanceAnalyzer.getBasicBlocks());

            // eliminating SSA from code
            ssaConverter.toNormalForm();

            DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\normal_cfg_post_ssa_" + sequence.function + ".dot",
                    "C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\normal_rcfg_post_ssa_" + sequence.function + ".dot",
                    dominanceAnalyzer.getBasicBlocks());
        }
    }

    public List<CodeSequence> getOptimizationOutput() {
        return codeSequenceList;
    }

    public static List<BasicBlock> reassembleBasicBlocks(List<BasicBlock> cfg) {
        /*List<BasicBlock> result = new ArrayList<>();


        Stack<BasicBlock> stack = new Stack<>();
        BasicBlock currentBlock = cfg.stream().filter(BasicBlock::isEntryBlock).collect(Collectors.toList()).get(0);
        // current block is the entry block

        int index = 0;
        while (index < cfg.size()) {
            if (cfg.size() == 1 || currentBlock.successor.size() == 0)
                result.add(currentBlock);
            else if (currentBlock.successor.size() == 1) {
                Quadruple lastInstruction = currentBlock.instructions.get(currentBlock.instructions.size() - 1);

                result.add(currentBlock);
                if (lastInstruction.getInstruction() == IRInstruction.JMP)
                    currentBlock = stack.pop();
                else
                    currentBlock = currentBlock.successor.get(0);
            } else if (currentBlock.successor.size() == 2) {
                Quadruple lastInstruction = currentBlock.instructions.get(currentBlock.instructions.size() - 1);

                QuadrupleLabel jumpDestination = (QuadrupleLabel) lastInstruction.getResult();

                BasicBlock successor1 = currentBlock.successor.get(0);
                BasicBlock successor2 = currentBlock.successor.get(1);

                BasicBlock addToStack = successor2;
                for (int i = 0; i < successor1.instructions.size(); i++) {
                    Quadruple q = successor1.instructions.get(i);
                    if (q.getInstruction() == IRInstruction.GEN_LABEL) {
                        if (((QuadrupleLabel) q.getArg1()).getLabelName().equals(jumpDestination.getLabelName())) {
                            addToStack = successor1;
                            break;
                        }
                    }
                }

                if (stack.isEmpty() || stack.peek() != addToStack)
                    stack.push(addToStack);
                result.add(currentBlock);
                currentBlock = (addToStack == successor1 ? successor2 : successor1);
            } else
                throw new RuntimeException("Basic block cannot have more than two successors.");

            // solution for IF without ELSE
            if (!stack.isEmpty() && stack.peek() == currentBlock)
                stack.pop();

            index++;
        }

        assert result.size() == result.stream().distinct().count();*/

        return cfg;
    }
}
