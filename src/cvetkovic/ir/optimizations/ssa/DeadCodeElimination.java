package cvetkovic.ir.optimizations.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleARR;
import cvetkovic.ir.quadruple.arguments.QuadrupleLabel;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadruplePhi;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;
import java.util.stream.Collectors;

public class DeadCodeElimination implements OptimizerPass {

    private final DominanceAnalyzer dominanceAnalyzer;
    private final Set<Quadruple> marked = new HashSet<>();

    public DeadCodeElimination(DominanceAnalyzer dominanceAnalyzer) {
        this.dominanceAnalyzer = dominanceAnalyzer;

        // TODO: check if code is in SSA form
    }

    @Override
    public void optimize() {
        mark();
        sweep();
    }

    @Override
    public void finalizePass() {
        // simplify CFG
        // removal of unreachable blocks
    }

    /**
     * Critical statements are I/O statements, linkage code (entry & exit blocks),
     * return values, calls to other procedures.
     */
    private boolean isCritical(Quadruple instruction) {
        switch (instruction.getInstruction()) {
            // I/O statements
            case SCANF:
            case PRINTF:
                return true;
            // linkage code
            case GEN_LABEL:
            case ENTER:
            case LEAVE:
            case RETURN:
                return true;
            // calls to other procedures
            case PARAM:
            case CALL:
            case INVOKE_VIRTUAL:
                return true;
            // other
            default:
                return false;
        }
    }

    /**
     * Algorithm starts from the critical instructions and then iteratively
     * marks instruction whose results are used.
     */
    private void mark() {
        Set<Quadruple> worklist = new HashSet<>();

        Map<Obj, Set<Quadruple>> defined = new HashMap<>();
        Map<Quadruple, BasicBlock> quadrupleBelongsTo = new HashMap<>();

        for (BasicBlock block : dominanceAnalyzer.getBasicBlocks()) {
            for (Quadruple q : block.instructions) {
                // building the defined map
                quadrupleBelongsTo.put(q, block);

                if (q.getResult() != null && q.getResult() instanceof QuadrupleObjVar) {
                    Obj result = ((QuadrupleObjVar) q.getResult()).getObj();

                    Set<Quadruple> set;
                    if (defined.containsKey(result))
                        set = defined.get(result);
                    else {
                        set = new HashSet<>();
                        defined.put(result, set);
                    }

                    set.add(q);
                }

                // TODO: what to do with instructions that alter the global variables

                // mark only critical instructions
                if (isCritical(q)) {
                    marked.add(q);

                    if (q.getInstruction() != IRInstruction.GEN_LABEL)
                        worklist.add(q);
                }
            }
        }

        while (!worklist.isEmpty()) {
            Quadruple instruction = worklist.iterator().next();
            worklist.remove(instruction);

            if (instruction.getArg1() != null) {
                if (instruction.getArg1() instanceof QuadrupleObjVar) {
                    // if def(y) is not marked
                    Obj arg1 = ((QuadrupleObjVar) instruction.getArg1()).getObj();
                    if (arg1.getKind() != Obj.Con) {
                        Set<Quadruple> defSet = defined.get(arg1);

                        // defSet will be null when 'arg1' is function parameter or global variable
                        if (defSet != null) {
                            Quadruple c = defSet.stream().filter(p -> p.getSsaResultCount() == instruction.getSsaArg1Count()).findFirst().orElseThrow();
                            if (!marked.contains(c)) {
                                marked.add(c);
                                worklist.add(c);
                            }
                        }
                    }
                } else if (instruction.getArg1() instanceof QuadruplePhi) {
                    QuadruplePhi phiFunction = (QuadruplePhi) instruction.getArg1();
                    Obj obj = phiFunction.getObj();

                    if (obj.getKind() != Obj.Con) {
                        Set<Quadruple> defSet = defined.get(obj);

                        for (int i = 0; i < phiFunction.size(); i++) {
                            // first assignment is defined outside of function -> parameter or global variable
                            if (phiFunction.getPhiArg(i) == 0)
                                continue;

                            int finalI = i;
                            Quadruple c = defSet.stream().filter(p -> p.getSsaResultCount() == phiFunction.getPhiArg(finalI)).findFirst().orElseThrow();
                            if (!marked.contains(c)) {
                                marked.add(c);
                                worklist.add(c);
                            }
                        }
                    }
                }
            }

            if (instruction.getArg2() != null && instruction.getArg2() instanceof QuadrupleObjVar) {
                // if def(z) is not marked
                Obj arg2 = ((QuadrupleObjVar) instruction.getArg2()).getObj();
                if (arg2.getKind() != Obj.Con) {
                    Set<Quadruple> defSet = defined.get(arg2);

                    // defSet will be null when 'arg2' is function parameter or global variable
                    if (defSet != null) {
                        List<Quadruple> c = defSet.stream().filter(p -> p.getSsaResultCount() == instruction.getSsaArg2Count()).collect(Collectors.toList());
                        if (!c.isEmpty()) {
                            marked.add(c.get(0));
                            worklist.add(c.get(0));
                        }
                    }
                }
            }

            Map<BasicBlock, Set<BasicBlock>> RDF = dominanceAnalyzer.getReverseDominanceFrontier();
            BasicBlock quadrupleIsIn = quadrupleBelongsTo.get(instruction);
            for (BasicBlock b : RDF.get(quadrupleIsIn)) {
                Quadruple cmp = b.instructions.get(b.instructions.size() - 2);
                Quadruple branch = b.instructions.get(b.instructions.size() - 1);

                assert cmp.getInstruction() == IRInstruction.CMP;
                assert IRInstruction.isConditionalJumpInstruction(branch.getInstruction());

                if (!marked.contains(cmp) && !marked.contains(branch)) {
                    marked.add(cmp);
                    marked.add(branch);

                    worklist.add(cmp);
                    worklist.add(branch);
                }
            }

            assert (long) worklist.size() == worklist.stream().distinct().count();
        }
    }

    /**
     * Does the deletion of instructions that are not marked and not jump instructions.
     * If non-marked instruction is a jump instruction then route the jump to the
     * nearest useful post-dominator.
     */
    private void sweep() {
        for (BasicBlock block : dominanceAnalyzer.getBasicBlocks()) {
            Set<Quadruple> toRemove = new HashSet<>();

            boolean branchRemoved = false;
            for (Quadruple q : block.instructions) {
                if (marked.contains(q))
                    continue;

                if (IRInstruction.isConditionalJumpInstruction(q.getInstruction())) {
                    toRemove.add(block.instructions.get(block.instructions.size() - 2)); // CMP
                    toRemove.add(block.instructions.get(block.instructions.size() - 1)); // conditional branch
                    branchRemoved = true;
                } else if (IRInstruction.isUnconditionalJumpInstruction(q.getInstruction())) {
                    continue;
                } else {
                    toRemove.add(q);
                }
            }

            block.instructions.removeAll(toRemove);
            if (branchRemoved) {
                BasicBlock successor = dominanceAnalyzer.getReverseImmediateDominators().get(block);

                Quadruple rewrittenJump = new Quadruple(IRInstruction.JMP);
                rewrittenJump.setResult(new QuadrupleLabel(successor.instructions.get(0).getArg1().toString()));
                block.instructions.add(rewrittenJump);

                block.successors.clear();
                block.successors.add(successor);
            }
        }
    }
}
