package cvetkovic.optimizer.passes;

import cvetkovic.ir.BasicBlock;
import cvetkovic.ir.CodeSequence;
import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleLabel;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadruplePTR;
import cvetkovic.ir.quadruple.arguments.QuadruplePhi;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;

public class DeadCodeElimination implements OptimizerPass {

    private final CodeSequence sequence;

    public DeadCodeElimination(CodeSequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public void optimize() {
        Set<Quadruple> marked = mark(sequence);
        sweep(marked);
    }

    @Override
    public void finalizePass() {
        // NOTE: do not calculate dominance relations here, but instead run CFGCleaner pass after this one
    }

    /**
     * Critical statements are I/O statements, linkage code (entry & exit blocks),
     * return values, calls to other procedures.
     */
    private static boolean isCritical(Quadruple instruction) {
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
            // array
            case MALLOC:
            case ALOAD:
            case ASTORE:
                return true;
            // pointer store
            case STORE:
                if (instruction.getArg2() != null && instruction.getArg2() instanceof QuadruplePTR)
                    return true;
            default:
                return false;
        }
    }

    /**
     * Algorithm starts from the critical instructions and then iteratively
     * marks instruction whose results are used.
     */
    static Set<Quadruple> mark(CodeSequence sequence) {
        Set<Quadruple> marked = new HashSet<>();
        Set<Quadruple> worklist = new HashSet<>();

        Map<Obj, Set<Quadruple>> defined = new HashMap<>();
        Map<Quadruple, BasicBlock> quadrupleBelongsTo = new HashMap<>();

        for (BasicBlock block : sequence.dominanceAnalyzer.getBasicBlocks()) {
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

                // mark only critical instructions
                if (isCritical(q)) {
                    marked.add(q);

                    if (q.getInstruction() != IRInstruction.GEN_LABEL &&
                            q.getInstruction() != IRInstruction.ENTER &&
                            q.getInstruction() != IRInstruction.LEAVE)
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
                        if (defSet != null && instruction.getSsaArg1Count() != 0) {
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
                    if (defSet != null && instruction.getSsaArg2Count() != 0) {
                        Quadruple c = defSet.stream().filter(p -> p.getSsaResultCount() == instruction.getSsaArg2Count()).findFirst().orElseThrow();
                        if (!marked.contains(c)) {
                            marked.add(c);
                            worklist.add(c);
                        }
                    }
                }
            }

            // instructions that use pointers need to be handled separately
            if ((instruction.getInstruction() == IRInstruction.STORE || instruction.getInstruction() == IRInstruction.MALLOC)
                    && instruction.getArg2() != null &&
                    instruction.getArg2() instanceof QuadruplePTR) {
                Obj result = ((QuadrupleObjVar) instruction.getResult()).getObj();
                Set<Quadruple> defSet = defined.get(result);

                // defSet will be null when 'result' is function parameter or global variable
                if (defSet != null) {
                    Quadruple c = defSet.stream().filter(p -> p.getSsaResultCount() == instruction.getSsaResultCount() - 1).findFirst().orElseThrow();
                    if (!marked.contains(c)) {
                        marked.add(c);
                        worklist.add(c);
                    }
                }
            }

            Map<BasicBlock, Set<BasicBlock>> RDF = sequence.dominanceAnalyzer.getReverseDominanceFrontier();
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

        return marked;
    }

    /**
     * Does the deletion of instructions that are not marked and not jump instructions.
     * If non-marked instruction is a jump instruction then route the jump to the
     * nearest useful post-dominator.
     */
    private void sweep(Set<Quadruple> marked) {
        for (BasicBlock block : sequence.dominanceAnalyzer.getBasicBlocks()) {
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
                BasicBlock successor = findFirstUsefulPostdominatorBlock(block);

                Quadruple rewrittenJump = new Quadruple(IRInstruction.JMP);
                rewrittenJump.setResult(new QuadrupleLabel(successor.instructions.get(0).getArg1().toString()));
                block.instructions.add(rewrittenJump);

                for (BasicBlock s : block.successors)
                    s.predecessors.remove(block);
                successor.predecessors.add(block);

                block.successors.clear();
                block.successors.add(successor);
            }
        }
    }

    private BasicBlock findFirstUsefulPostdominatorBlock(BasicBlock source) {
        Map<BasicBlock, BasicBlock> previous = new HashMap<>();
        Map<BasicBlock, Integer> distance = new HashMap<>();
        PriorityQueue<BasicBlock> Q = new PriorityQueue<>((o1, o2) -> {
            int distance1 = distance.get(o1);
            int distance2 = distance.get(o2);

            return Integer.compare(distance1, distance2);
        });

        for (BasicBlock v : sequence.basicBlocks) {
            if (v == source)
                distance.put(source, 0);
            else
                distance.put(v, Integer.MAX_VALUE - 1);
            previous.put(v, null);
            Q.add(v);
        }

        while (!Q.isEmpty()) {
            BasicBlock u = Q.poll();

            // we look at RCFG, hence predecessors
            for (BasicBlock v : u.successors) {
                if (!Q.contains(v))
                    continue;

                int alt = distance.get(u) + 1;
                if (alt < distance.get(v)) {
                    distance.put(v, alt);
                    previous.put(v, u);
                }
            }
        }

        // postdominators = reverse dominators
        List<BasicBlock> rdomSorted = new ArrayList<>(sequence.dominanceAnalyzer.getReverseDominators().get(source));
        rdomSorted.remove(source);
        rdomSorted.sort(Comparator.comparing(distance::get));

        assert rdomSorted.size() > 0;
        if (rdomSorted.size() > 1)
            assert distance.get(rdomSorted.get(0)) <= distance.get(rdomSorted.get(1));

        // getting nearest non-empty postdominator
        BasicBlock toReturn = null;
        for (int i = 0; i < rdomSorted.size(); i++) {
            if (!rdomSorted.get(i).isEmpty()) {
                toReturn = rdomSorted.get(i);
                break;
            }
        }

        return toReturn;
    }
}
