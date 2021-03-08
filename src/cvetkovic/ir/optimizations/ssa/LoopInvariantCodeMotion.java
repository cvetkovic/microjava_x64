package cvetkovic.ir.optimizations.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleLabel;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
            tuple.v.forEach(p -> definedIn.addAll(p.getSetOfSSADefinedVariables()));

            BasicBlock header = tuple.u;
            BasicBlock preheader = new BasicBlock(sequence.function);

            boolean changed;

            do {
                BasicBlock.Tuple<BasicBlock, Quadruple> loopInvariantInstruction = findInvariantInstruction(tuple.v, definedIn);

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

            definedIn.remove(definedIn.stream().
                    filter(p -> (p.u == resultObj) && (p.v == ssaCount)).
                    findFirst().orElseThrow());
        }
    }

    private BasicBlock.Tuple<BasicBlock, Quadruple> findInvariantInstruction(Set<BasicBlock> basicBlocks,
                                                                             Set<BasicBlock.Tuple<Obj, Integer>> definedIn) {
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
                            continue;
                    }
                }

                if (q.getArg1() instanceof QuadrupleObjVar) {
                    Obj arg1 = ((QuadrupleObjVar) q.getArg1()).getObj();
                    int ssaCnt = q.getSsaArg1Count();

                    if (arg1.getKind() != Obj.Var && arg1.getKind() != Obj.Fld && arg1.getKind() == Obj.Con)
                        continue;

                    if (arg1.getKind() == Obj.Con || definedIn.stream().noneMatch(p -> p.u == arg1 && p.v == ssaCnt))
                        invariant1 = true;
                }

                if (q.getArg2() instanceof QuadrupleObjVar) {
                    Obj arg2 = ((QuadrupleObjVar) q.getArg2()).getObj();
                    int ssaCnt = q.getSsaArg2Count();

                    if (arg2.getKind() != Obj.Var && arg2.getKind() != Obj.Fld && arg2.getKind() != Obj.Con)
                        continue;

                    if (arg2.getKind() == Obj.Con || definedIn.stream().noneMatch(p -> p.u == arg2 && p.v == ssaCnt))
                        invariant2 = true;
                }

                if ((invariant1 && q.getArg2() == null) || (invariant1 && invariant2))
                    return new BasicBlock.Tuple<>(block, q);
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
}
