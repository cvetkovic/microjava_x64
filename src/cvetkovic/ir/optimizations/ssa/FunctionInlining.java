package cvetkovic.ir.optimizations.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;

public class FunctionInlining implements OptimizerPass {

    private CodeSequence currentSequence;
    private List<CodeSequence> program;

    private int cnt = 0;

    public FunctionInlining(CodeSequence currentSequence, List<CodeSequence> program) {
        this.currentSequence = currentSequence;
        this.program = program;
    }

    private boolean allowToInline(Obj function) {
        CodeSequence inlinedFunction = program.stream().filter(p -> p.function == function).findFirst().orElseThrow();

        boolean allow = true;
        for (BasicBlock b : inlinedFunction.basicBlocks) {
            for (Quadruple q : b.instructions) {
                if (q.getInstruction() == IRInstruction.CALL || q.getInstruction() == IRInstruction.INVOKE_VIRTUAL) {
                    allow = false;
                    break;
                }
            }
        }

        // TODO: check that function has less basic blocks than the callee

        return allow;
    }

    private Set<BasicBlock.Tuple<BasicBlock, Quadruple>> getCallsToInline() {
        Set<BasicBlock.Tuple<BasicBlock, Quadruple>> result = new HashSet<>();

        for (BasicBlock block : currentSequence.basicBlocks) {
            for (Quadruple instruction : block.instructions) {
                if (instruction.getInstruction() == IRInstruction.CALL ||
                        instruction.getInstruction() == IRInstruction.INVOKE_VIRTUAL) {
                    Obj functionObj = ((QuadrupleObjVar) instruction.getArg1()).getObj();

                    if (allowToInline(functionObj))
                        result.add(new BasicBlock.Tuple<>(block, instruction));
                }
            }
        }

        return result;
    }

    private List<Quadruple> getCallParameters(BasicBlock block, Quadruple call) {
        List<Quadruple> result = new ArrayList<>();

        int indexOfCall = block.instructions.indexOf(call);
        if (block.instructions.get(indexOfCall - 1).getInstruction() == IRInstruction.PARAM) {
            for (int i = indexOfCall - 1; i >= 0; i--) {
                Quadruple q = block.instructions.get(i);
                if (q.getInstruction() == IRInstruction.PARAM)
                    result.add(0, q);
                else
                    break;
            }
        }

        return result;
    }

    private List<BasicBlock> cloneCFG(Quadruple v, int startCntFrom) {
        List<BasicBlock> result = new ArrayList<>();

        Obj functionObj = ((QuadrupleObjVar) v.getArg1()).getObj();
        CodeSequence sequenceToInline = program.stream().filter(p -> p.function == functionObj).findFirst().orElseThrow();

        Map<BasicBlock, BasicBlock> mappingsToClones = new HashMap<>();

        // cloning basic blocks
        for (BasicBlock block : sequenceToInline.basicBlocks) {
            BasicBlock clone = block.makeClone();
            clone.blockId = startCntFrom++;

            // TODO: change label names to be unique

            result.add(clone);
            mappingsToClones.put(block, clone);
        }

        // reconnecting CFG
        for (BasicBlock block : sequenceToInline.basicBlocks) {
            BasicBlock cloned = mappingsToClones.get(block);

            // successors
            for (BasicBlock b : block.successors)
                cloned.successors.add(mappingsToClones.get(b));

            // predecessors
            for (BasicBlock b : block.predecessors)
                cloned.predecessors.add(mappingsToClones.get(b));
        }

        return result;
    }

    @Override
    public void optimize() {
        Set<BasicBlock.Tuple<BasicBlock, Quadruple>> placesToInline = getCallsToInline();

        for (BasicBlock.Tuple<BasicBlock, Quadruple> tuple : placesToInline) {
            List<Quadruple> callParameters = getCallParameters(tuple.u, tuple.v);

            int maxBlockCnt = currentSequence.basicBlocks.stream().mapToInt(p -> p.blockId).max().orElseThrow();
            List<BasicBlock> clonedCFG = cloneCFG(tuple.v, maxBlockCnt + 1);

            cnt++;
        }
    }

    public void finalizePass() {
        if (cnt > 0)
            currentSequence.dominanceAnalyzer = new DominanceAnalyzer(currentSequence);
    }
}
