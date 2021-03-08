package cvetkovic.ir.optimizations.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.*;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class LoopInvariantCodeMotion implements OptimizerPass {

    private CodeSequence sequence;
    private DominanceAnalyzer dominanceAnalyzer;
    private int invariantsFound = 0;

    public LoopInvariantCodeMotion(CodeSequence sequence, DominanceAnalyzer dominanceAnalyzer) {
        this.sequence = sequence;
        this.dominanceAnalyzer = dominanceAnalyzer;
    }

    @Override
    public void optimize() {
        List<BasicBlock.Tuple<BasicBlock, Set<BasicBlock>>> loops = dominanceAnalyzer.getNaturalLoops();

        for (BasicBlock.Tuple<BasicBlock, Set<BasicBlock>> tuple : loops) {
            Set<BasicBlock.Tuple<Obj, Integer>> definedIn = new HashSet<>();
            tuple.v.forEach(p -> definedIn.addAll(p.getSetOfSSADefinedVariablesWithNegatedPHIs()));

            Map<Obj, Set<Quadruple>> phis = new HashMap<>();
            for (BasicBlock b : tuple.v) {
                for (Quadruple q : b.instructions) {
                    if (q.getInstruction() == IRInstruction.STORE_PHI) {
                        QuadruplePhi phi = (QuadruplePhi) q.getArg1();

                        if (phis.containsKey(phi.getObj()))
                            phis.get(phi.getObj()).add(q);
                        else {
                            Set<Quadruple> set = new HashSet<>();
                            set.add(q);
                            phis.put(phi.getObj(), set);
                        }
                    }
                }
            }

            BasicBlock header = tuple.u;
            BasicBlock preheader = new BasicBlock(sequence.function);

            boolean changed;

            do {
                BasicBlock.Tuple<BasicBlock, Quadruple> loopInvariantInstruction = findInvariantInstruction(tuple.v, definedIn, tuple.v, phis, tuple.u);

                if (loopInvariantInstruction != null) {
                    addToPreheader(loopInvariantInstruction, preheader, definedIn);

                    changed = true;
                    invariantsFound++;
                } else
                    changed = false;
            } while (changed);

            if (invariantsFound > 0) {
                embedPreheaderIntoCFG(preheader, header, tuple.v);
            }
        }
    }

    private static int invariantCnt = 0;

    private void embedPreheaderIntoCFG(BasicBlock preheader, BasicBlock header, Set<BasicBlock> loop) {
        QuadrupleLabel preheaderLabel = new QuadrupleLabel("INVARIANT_" + invariantCnt++);

        Quadruple preHeaderGenLabel = new Quadruple(IRInstruction.GEN_LABEL);
        preHeaderGenLabel.setArg1(preheaderLabel);
        preheader.instructions.add(0, preHeaderGenLabel);

        Quadruple preHeaderJMP = new Quadruple(IRInstruction.JMP);
        preHeaderJMP.setResult(new QuadrupleLabel(header.getLabelName()));
        preheader.instructions.add(preHeaderJMP);

        for (BasicBlock predecessor : header.predecessors) {
            if (!loop.contains(predecessor)) {
                preheader.predecessors.add(predecessor);
                predecessor.successors.remove(header);
                predecessor.successors.add(preheader);

                Quadruple lastInstruction = predecessor.getLastInstruction();

                if (IRInstruction.isUnconditionalJumpInstruction(lastInstruction.getInstruction()))
                    lastInstruction.setResult(new QuadrupleLabel(preheaderLabel.getLabelName()));
                else if (IRInstruction.isConditionalJumpInstruction(lastInstruction.getInstruction())) {
                    throw new RuntimeException("Not yet implemented."); // TODO: implement this
                } else
                    throw new RuntimeException("Invalid IR.");
            }
        }

        header.predecessors.removeAll(header.predecessors.stream().filter(p -> !loop.contains(p)).collect(Collectors.toSet()));
        header.predecessors.add(preheader);

        preheader.successors.add(header);
        preheader.blockId = sequence.basicBlocks.stream().mapToInt(p -> p.blockId).max().orElseThrow() + 1;
        sequence.basicBlocks.add(preheader);

        preheader.allVariables = preheader.extractAllVariables();
    }

    private void addToPreheader(BasicBlock.Tuple<BasicBlock, Quadruple> loopInvariantInstruction,
                                BasicBlock preheader,
                                Set<BasicBlock.Tuple<Obj, Integer>> definedIn) {
        Quadruple toRemove = loopInvariantInstruction.v;

        preheader.instructions.add(toRemove);
        loopInvariantInstruction.u.instructions.remove(toRemove);

        if (toRemove.getResult() instanceof QuadrupleObjVar) {
            Obj resultObj = ((QuadrupleObjVar) toRemove.getResult()).getObj();
            int ssaCount = toRemove.getSsaResultCount();

            if (!(toRemove.getArg2() instanceof QuadruplePTR) && !(toRemove.getArg2() instanceof QuadrupleARR))
                definedIn.remove(definedIn.stream().
                        filter(p -> (p.u == resultObj) && (Math.abs(p.v) == ssaCount)).
                        findFirst().orElseThrow());
        }
    }

    private BasicBlock.Tuple<BasicBlock, Quadruple> findInvariantInstruction(Set<BasicBlock> basicBlocks,
                                                                             Set<BasicBlock.Tuple<Obj, Integer>> definedIn,
                                                                             Set<BasicBlock> loop,
                                                                             Map<Obj, Set<Quadruple>> phis,
                                                                             BasicBlock loopHeader) {
        for (BasicBlock block : basicBlocks) {
            for (Quadruple q : block.instructions) {
                boolean invariant1 = false;
                boolean invariant2 = false;

                if (IRInstruction.isJumpInstruction(q.getInstruction()))
                    continue;
                else {
                    switch (q.getInstruction()) {
                        case SCANF:
                        case PRINTF:
                        case PARAM:
                        case CALL:
                        case INVOKE_VIRTUAL:
                        case GEN_LABEL:
                        case CMP:
                        case STORE_PHI:
                            continue;
                    }
                }

                if (q.getArg1() instanceof QuadrupleObjVar) {
                    Obj arg1 = ((QuadrupleObjVar) q.getArg1()).getObj();
                    int ssaCnt = q.getSsaArg1Count();

                    /*if (arg1.getKind() != Obj.Var && arg1.getKind() != Obj.Fld && arg1.getKind() != Obj.Con)
                        continue;*/

                    if (arg1.getKind() == Obj.Con || definedIn.stream().noneMatch(p -> p.u == arg1 && Math.abs(p.v) == ssaCnt))
                        invariant1 = true;
                }

                if (q.getArg2() instanceof QuadrupleObjVar) {
                    Obj arg2 = ((QuadrupleObjVar) q.getArg2()).getObj();
                    int ssaCnt = q.getSsaArg2Count();

                    /*if (arg2.getKind() != Obj.Var && arg2.getKind() != Obj.Fld && arg2.getKind() != Obj.Con)
                        continue;*/

                    if (arg2.getKind() == Obj.Con || definedIn.stream().noneMatch(p -> p.u == arg2 && Math.abs(p.v) == ssaCnt))
                        invariant2 = true;
                } else if (q.getArg2() instanceof QuadruplePTR)
                    invariant2 = true;

                if ((invariant1 && q.getArg2() == null) || (invariant1 && invariant2)) {
                    if (satisfiesHostingCriteria(block,
                            loop,
                            definedIn,
                            ((QuadrupleObjVar) q.getResult()).getObj(),
                            q.getSsaResultCount(),
                            phis,
                            loopHeader))
                        return new BasicBlock.Tuple<>(block, q);
                }
            }
        }

        return null;
    }

    @Override
    public void finalizePass() {
        if (invariantsFound > 0) {
            sequence.dominanceAnalyzer = new DominanceAnalyzer(sequence);
        }
    }

    private boolean satisfiesHostingCriteria(BasicBlock block,
                                             Set<BasicBlock> loop,
                                             Set<BasicBlock.Tuple<Obj, Integer>> definedIn,
                                             Obj resultObj,
                                             int ssaCount,
                                             Map<Obj, Set<Quadruple>> phis,
                                             BasicBlock loopHeader) {
        BasicBlock blockNotInLoop = null;
        for (BasicBlock loopHeaderSuccessor : loopHeader.successors) {
            if (!loop.contains(loopHeaderSuccessor)) {
                blockNotInLoop = loopHeaderSuccessor;
                break;
            }
        }
        assert blockNotInLoop != null;

        BasicBlock loopExitPredecessorInLoop = null;
        for (BasicBlock loopHeaderPredecessor : loopHeader.successors) {
            if (loop.contains(loopHeaderPredecessor)) {
                loopExitPredecessorInLoop = loopHeaderPredecessor;
                break;
            }
        }
        assert loopExitPredecessorInLoop != null;

        boolean criterion1 = false, criterion2 = true, criterion3 = true;

        // criterion 1 - 'block' dominates all loop exits
        // NOTE: this criteria forbids elimination from if-then-else structures
        Set<BasicBlock> dominatorsOfBlock = dominanceAnalyzer.getDominators().get(loopExitPredecessorInLoop);
        if (dominatorsOfBlock.contains(block))
            criterion1 = true;

        // criterion 2 - quadruple's result not defined elsewhere in L
        // NOTE: this criteria forbids elimination from if-then structures
        long numberOfDefinitions = definedIn.stream().filter(p -> p.u == resultObj && p.v > 0).count();
        numberOfDefinitions -= phis.getOrDefault(resultObj, new HashSet<>()).size();
        if (numberOfDefinitions > 1)
            criterion2 = false;

        // criterion 3 - all uses
        // NOTE: this criteria forbids elimination from if-then structures
        criterion3 = thirdCriterion(loop, resultObj, ssaCount, phis);
        /*Set<Quadruple> phi_set = phis.getOrDefault(resultObj, new HashSet<>());
        for (Quadruple q : phi_set) {
            QuadruplePhi phi = (QuadruplePhi) q.getArg1();

            int phisFromThisLoop = 0;
            Set tmp = definedIn.stream().filter(p -> p.u == resultObj).map(value -> Math.abs(value.v)).collect(Collectors.toSet());
            tmp.remove(q.getSsaResultCount());

            if (phi.contains(ssaCount) && !phi.contains(0) && phisFromThisLoop > 1) {
                criterion3 = false;
                break;
            }
        }*/

        return criterion1 & criterion2 & criterion3;
    }

    private boolean thirdCriterion(Set<BasicBlock> loop, Obj obj, int ssaCnt, Map<Obj, Set<Quadruple>> phis) {
        for (BasicBlock block : loop) {
            for (Quadruple q : block.instructions) {
                if (q.getArg1() instanceof QuadrupleObjVar && ((QuadrupleObjVar) q.getArg1()).getObj() == obj) {
                    Set<Quadruple> phi_instructions = phis.getOrDefault(obj, new HashSet<>());
                    Quadruple particularPhi = phi_instructions.stream().
                            filter(p -> p.getSsaResultCount() == q.getSsaArg1Count()).findFirst().orElse(null);

                    if (particularPhi != null && ((QuadruplePhi) particularPhi.getArg1()).contains(ssaCnt))
                        return false;
                }

                if (q.getArg2() instanceof QuadrupleObjVar && ((QuadrupleObjVar) q.getArg2()).getObj() == obj) {
                    Set<Quadruple> phi_instructions = phis.getOrDefault(obj, new HashSet<>());
                    Quadruple particularPhi = phi_instructions.stream().
                            filter(p -> p.getSsaResultCount() == q.getSsaArg2Count()).findFirst().orElse(null);

                    if (particularPhi != null && ((QuadruplePhi) particularPhi.getArg2()).contains(ssaCnt))
                        return false;
                }

                // TODO: recursive phi resolving here
            }
        }

        return true;
    }
}
