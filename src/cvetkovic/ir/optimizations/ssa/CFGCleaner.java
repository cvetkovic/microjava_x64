package cvetkovic.ir.optimizations.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleLabel;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.OptimizerPass;

import java.util.*;

public class CFGCleaner implements OptimizerPass {

    private final CodeSequence sequence;

    public CFGCleaner(CodeSequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public void optimize() {
        boolean changed;

        do {
            changed = cleanUnreachableBlocks();
        } while (changed);

        do {
            List<BasicBlock> postorderOnCFG = generatePostorder();
            changed = onePass(postorderOnCFG);
        } while (changed);
    }

    /**
     * Deletes blocks with no predecessors
     */
    public boolean cleanUnreachableBlocks() {
        Set<BasicBlock> toDelete = new HashSet<>();

        for (BasicBlock b : sequence.basicBlocks) {
            if (b.predecessors.size() == 0 && !b.isEntryBlock()) {
                toDelete.add(b);
            }
        }

        // remove predecessor link from blocks that have been removed
        for (BasicBlock b : sequence.basicBlocks)
            b.predecessors.removeAll(toDelete);
        sequence.basicBlocks.removeAll(toDelete);

        return toDelete.size() != 0;
    }

    private List<BasicBlock> generatePostorder() {
        Stack<BasicBlock> stack = new Stack<>();
        Set<BasicBlock> visited = new HashSet<>();

        List<BasicBlock> result = new ArrayList<>();

        BasicBlock next = sequence.entryBlock;
        stack.push(next);

        while (!stack.isEmpty()) {
            next = stack.peek();

            if (next.successors.size() == 0 || visited.contains(next)) {
                BasicBlock X = stack.pop();

                if (!result.contains(X))
                    result.add(X);
            } else {
                next.successors.forEach(stack::push);
                visited.add(next);
            }
        }

        return result;
    }

    private boolean onePass(List<BasicBlock> postorder) {
        boolean changed = false;

        Set<BasicBlock> toRemove = new HashSet<>();

        // postorder on CFG
        for (BasicBlock i : postorder) {
            if (toRemove.contains(i))
                continue;

            Quadruple lastInstruction = i.instructions.get(i.instructions.size() - 1);

            if (IRInstruction.isConditionalJumpInstruction(lastInstruction.getInstruction())) {
                QuadrupleLabel dest1 = (QuadrupleLabel) lastInstruction.getArg2();
                QuadrupleLabel dest2 = (QuadrupleLabel) lastInstruction.getResult();

                if (dest1.getLabelName().equals(dest2.getLabelName())) {
                    String labelName = dest1.getLabelName();

                    i.instructions.remove(i.instructions.size() - 1);
                    i.instructions.remove(i.instructions.size() - 1);

                    Quadruple newJump = new Quadruple(IRInstruction.JMP);
                    newJump.setResult(new QuadrupleLabel(labelName));

                    i.instructions.add(newJump);
                    i.successors.remove(0); // remove any
                    i.successors.get(0).predecessors.remove(i);
                    assert i.successors.size() == 1;

                    changed = true;
                }
            } else if (IRInstruction.isUnconditionalJumpInstruction(lastInstruction.getInstruction())) {
                BasicBlock successor = i.successors.get(0);

                // empty block removal
                if (i.isEmpty()) {
                    List<BasicBlock> predecessors = i.predecessors;

                    for (BasicBlock predecessor : predecessors)
                        removeEmptyBlock(predecessor, i, successor);
                    toRemove.add(i);

                    changed = true;
                }
                // successor of i has only one predecessor -> fuse i and j
                else if (successor.predecessors.size() == 1) {
                    fuseBlocks(i, successor);
                    changed = true;
                }
            }
        }

        sequence.basicBlocks.removeAll(toRemove);

        return changed;
    }

    private void fuseBlocks(BasicBlock current, BasicBlock successor) {
        // removing unconditional branch in current
        current.instructions.remove(current.getLastInstruction());
        // removing of GEN_LABEL
        successor.instructions.remove(0);

        // copying all instructions to current from successor
        current.instructions.addAll(successor.instructions);
        current.allVariables.addAll(successor.allVariables);

        // reconfiguring CFG
        current.successors.clear();
        current.successors.addAll(successor.successors);

        // removing fused block
        sequence.basicBlocks.remove(successor);

        for (BasicBlock successor_successors : successor.successors) {
            successor_successors.predecessors.remove(successor);
            successor_successors.predecessors.add(current);
        }
    }

    private void removeEmptyBlock(BasicBlock predecessor, BasicBlock current, BasicBlock successor) {
        Quadruple lastInstruction = predecessor.getLastInstruction();

        if (IRInstruction.isConditionalJumpInstruction(lastInstruction.getInstruction())) {
            QuadrupleLabel dest = (QuadrupleLabel) lastInstruction.getArg2();

            if (dest.getLabelName().equals(current.getLabelName()))
                lastInstruction.setArg2(new QuadrupleLabel(successor.getLabelName()));
            else
                lastInstruction.setResult(new QuadrupleLabel(successor.getLabelName()));
        } else if (IRInstruction.isUnconditionalJumpInstruction(lastInstruction.getInstruction())) {
            lastInstruction.setResult(new QuadrupleLabel(successor.getLabelName()));
        } else
            throw new RuntimeException("Invalid invocation of CFG cleaning method.");

        assert predecessor.successors.contains(current);
        predecessor.successors.remove(current);
        //if (!predecessor.successors.contains(successor))
        predecessor.successors.add(successor);

        successor.predecessors.remove(current);
        //if (!successor.predecessors.contains(predecessor))
        successor.predecessors.add(predecessor);

        //current.predecessors.clear();
        //current.successors.clear();
    }

    @Override
    public void finalizePass() {
        sequence.dominanceAnalyzer = new DominanceAnalyzer(sequence);
    }
}
