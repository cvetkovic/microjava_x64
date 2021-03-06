package cvetkovic.ir.optimizations.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Override
    public void optimize() {
        Set<BasicBlock.Tuple<BasicBlock, Quadruple>> placesToInline = getCallsToInline();

        for (BasicBlock.Tuple<BasicBlock, Quadruple> tuple : placesToInline) {
            List<Quadruple> getParams = getCallParameters(tuple.u, tuple.v);

            cnt++;
        }
    }

    public void finalizePass() {
        if (cnt > 0)
            currentSequence.dominanceAnalyzer = new DominanceAnalyzer(currentSequence);
    }
}
