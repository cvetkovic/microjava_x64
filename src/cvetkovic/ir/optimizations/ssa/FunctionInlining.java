package cvetkovic.ir.optimizations.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleLabel;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.misc.Config;
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

            // NOTE: labels are renamed to be unique in QuadrupleLabel.makeClone()

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
            BasicBlock leftoversFromCurrentBlock = createLeftoversBlock(tuple.u, tuple.v, ++maxBlockCnt);

            List<BasicBlock> clonedCFG = cloneCFG(tuple.v, maxBlockCnt + 1);
            BasicBlock inlinedStartBlock = clonedCFG.stream().filter(BasicBlock::isEntryBlock).findFirst().orElseThrow();
            BasicBlock inlinedEndBlock = clonedCFG.stream().filter(BasicBlock::isExitBlock).findFirst().orElseThrow();

            // TODO: fix call parameters
            // TODO: clean ENTER and LEAVE
            // TODO: see what to do with stack frame size -> change addresses
            embedFunction(tuple.u, tuple.v, inlinedStartBlock, inlinedEndBlock, leftoversFromCurrentBlock);
            addJumps(tuple.u, inlinedStartBlock, inlinedEndBlock, leftoversFromCurrentBlock);
            removeStackFrameInstructions(inlinedStartBlock, inlinedEndBlock);

            currentSequence.basicBlocks.add(leftoversFromCurrentBlock);
            currentSequence.basicBlocks.addAll(clonedCFG);

            cnt++;
            Config.inlinedCounter++;
        }
    }

    private void removeStackFrameInstructions(BasicBlock inlinedStartBlock, BasicBlock inlinedEndBlock) {
        Quadruple enterInstruction = inlinedStartBlock.instructions.
                stream().filter(p -> p.getInstruction() == IRInstruction.ENTER).findFirst().orElseThrow();
        inlinedStartBlock.instructions.remove(enterInstruction);

        Quadruple leaveInstruction = inlinedEndBlock.instructions.
                stream().filter(p -> p.getInstruction() == IRInstruction.LEAVE).findFirst().orElseThrow();
        inlinedEndBlock.instructions.remove(leaveInstruction);
    }

    private void addJumps(BasicBlock startFrom,
                          BasicBlock inlinedStartBlock,
                          BasicBlock inlinedEndBlock,
                          BasicBlock leftoversFromCurrentBlock) {
        Quadruple startFromJMP = new Quadruple(IRInstruction.JMP);
        startFromJMP.setResult(new QuadrupleLabel(inlinedStartBlock.getLabelName()));
        startFrom.instructions.add(startFromJMP);

        Quadruple inlinedEndBlockJMP = new Quadruple(IRInstruction.JMP);
        inlinedEndBlockJMP.setResult(new QuadrupleLabel(leftoversFromCurrentBlock.getLabelName()));
        inlinedEndBlock.instructions.add(inlinedEndBlockJMP);
    }

    private BasicBlock createLeftoversBlock(BasicBlock block, Quadruple instruction, int newBlockID) {
        int indexOfInstruction = block.instructions.indexOf(instruction);
        BasicBlock newBlock = new BasicBlock(block.enclosingFunction);

        newBlock.blockId = newBlockID;
        // TODO: set newBlock all variables

        Set<Quadruple> toRemove = new HashSet<>();

        Quadruple genVar = new Quadruple(IRInstruction.GEN_LABEL);
        genVar.setArg1(new QuadrupleLabel(Config.leftoversBlockPrefix + "_" + Config.leftoversLabelGenerator++));
        newBlock.instructions.add(genVar);

        for (int i = indexOfInstruction + 1; i < block.instructions.size(); i++) {
            newBlock.instructions.add(block.instructions.get(i));
            toRemove.add(block.instructions.get(i));
        }

        block.instructions.removeAll(toRemove);
        return newBlock;
    }

    private void embedFunction(BasicBlock startFrom,
                               Quadruple callInstruction,
                               BasicBlock inlinedStartBlock,
                               BasicBlock inlinedEndBlock,
                               BasicBlock leftoversFromCurrentBlock) {
        // link control flow graph vertices
        inlinedStartBlock.predecessors.add(startFrom);
        inlinedEndBlock.successors.add(leftoversFromCurrentBlock);

        leftoversFromCurrentBlock.predecessors.add(inlinedEndBlock);
        leftoversFromCurrentBlock.successors.addAll(startFrom.successors);

        startFrom.successors.clear();
        startFrom.successors.add(inlinedStartBlock);

        // TODO: move to a separate function
        // deleting call instruction
        startFrom.instructions.remove(callInstruction);
    }

    public void finalizePass() {
        if (cnt > 0)
            currentSequence.dominanceAnalyzer = new DominanceAnalyzer(currentSequence);
    }
}
