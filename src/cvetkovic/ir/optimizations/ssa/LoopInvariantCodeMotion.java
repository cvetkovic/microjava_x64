package cvetkovic.ir.optimizations.ssa;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoopInvariantCodeMotion implements OptimizerPass {

    private CodeSequence sequence;
    private DominanceAnalyzer dominanceAnalyzer;

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

            BasicBlock preheader = new BasicBlock(sequence.function);

            boolean changed = false;
            int cnt = 0;

            do {
                BasicBlock.Tuple<BasicBlock, Quadruple> loopInvariantInstruction = findInvariantInstruction(tuple.v, definedIn);

                addToPreheader(loopInvariantInstruction, preheader);
                removeFromBlock(loopInvariantInstruction);


                if (loopInvariantInstruction != null) {
                    changed = true;
                    cnt++;
                }
            } while (changed);

            if (cnt > 0) {
                // TODO: embed preheader into the CFG
            }
        }
    }

    private void addToPreheader(BasicBlock.Tuple<BasicBlock, Quadruple> loopInvariantInstruction,
                                BasicBlock preheader) {

    }

    private void removeFromBlock(BasicBlock.Tuple<BasicBlock, Quadruple> loopInvariantInstruction) {
    }

    private BasicBlock.Tuple<BasicBlock, Quadruple> findInvariantInstruction(Set<BasicBlock> basicBlocks,
                                                                             Set<BasicBlock.Tuple<Obj, Integer>> definedIn) {
        for (BasicBlock block : basicBlocks) {
            for (Quadruple q : block.instructions) {
                if (q.getArg1() instanceof QuadrupleObjVar) {
                    Obj arg1 = ((QuadrupleObjVar) q.getArg1()).getObj();

                }
            }
        }

        return null;
    }

    @Override
    public void finalizePass() {
        // TODO: recompute dominators
    }
}
