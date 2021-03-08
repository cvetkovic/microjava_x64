package cvetkovic.ir.optimizations.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.optimizations.IROptimizer;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleIntegerConst;
import cvetkovic.ir.quadruple.arguments.QuadrupleLabel;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import cvetkovic.misc.Config;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.OptimizerPass;
import cvetkovic.x64.SystemV_ABI;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;
import java.util.stream.Collectors;

public class FunctionInlining implements OptimizerPass {

    private final CodeSequence currentSequence;
    private final List<CodeSequence> program;

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

        if (allow)
            inlinedFunction.inlined = true;

        return allow;
    }

    private BasicBlock.Tuple<BasicBlock, Quadruple> getCallsToInline() {
        for (BasicBlock block : currentSequence.basicBlocks) {
            for (Quadruple instruction : block.instructions) {
                if (instruction.getInstruction() == IRInstruction.CALL ||
                        instruction.getInstruction() == IRInstruction.INVOKE_VIRTUAL) {
                    Obj functionObj = ((QuadrupleObjVar) instruction.getArg1()).getObj();

                    switch (functionObj.getName()) {
                        case "ord":
                        case "chr":
                        case "len":
                            continue;
                        default:
                            if (allowToInline(functionObj))
                                return new BasicBlock.Tuple<>(block, instruction);
                    }
                }
            }
        }

        return null;
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
            // NOTE: labels are renamed to be unique in QuadrupleLabel.makeClone()
            BasicBlock clone = block.makeClone();

            clone.blockId = startCntFrom++;
            //clone.allVariables = clone.extractAllVariables();

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
        BasicBlock.Tuple<BasicBlock, Quadruple> tuple;

        while ((tuple = getCallsToInline()) != null) {
            List<Quadruple> callParameters = getCallParameters(tuple.u, tuple.v);

            int addressOffset = ((QuadrupleIntegerConst) currentSequence.entryBlock.instructions.stream().
                    filter(p -> p.getInstruction() == IRInstruction.ENTER).findFirst().orElseThrow().getArg1()).getValue();
            Config.inlinedAddressOffset += addressOffset;

            int maxBlockCnt = currentSequence.basicBlocks.stream().mapToInt(p -> p.blockId).max().orElseThrow();
            BasicBlock leftoversFromCurrentBlock = createLeftoversBlock(tuple.u, tuple.v, ++maxBlockCnt);

            List<BasicBlock> clonedCFG = cloneCFG(tuple.v, maxBlockCnt + 1);
            BasicBlock inlinedStartBlock = clonedCFG.stream().filter(BasicBlock::isEntryBlock).findFirst().orElseThrow();
            BasicBlock inlinedEndBlock = clonedCFG.stream().filter(BasicBlock::isExitBlock).findFirst().orElseThrow();

            // TODO: see what to do with stack frame size -> change addresses fix
            // TODO: temps can overwrite some stack variables -> change address fix
            fixHolderEnterInstruction(addressOffset, inlinedStartBlock);
            embedFunction(tuple.u, tuple.v, inlinedStartBlock, inlinedEndBlock, leftoversFromCurrentBlock);
            addJumps(tuple.u, inlinedStartBlock, inlinedEndBlock, leftoversFromCurrentBlock);
            removeStackFrameInstructions(inlinedStartBlock, inlinedEndBlock);
            fixCallParameters(tuple.u, callParameters, tuple.v);

            currentSequence.basicBlocks.add(leftoversFromCurrentBlock);
            currentSequence.basicBlocks.addAll(clonedCFG);

            for (BasicBlock b : currentSequence.basicBlocks)
                // do not do extraction on block which calls inline function because copied parameters
                // must be inserted manually
                if (b != tuple.u)
                    b.allVariables = b.extractAllVariables();

            cnt++;
            Config.inlinedCounter++;
            QuadrupleObjVar.clonedRefs.clear();
        }
    }

    private void fixHolderEnterInstruction(int holder, BasicBlock inlinedStartBlock) {
        int inlined = ((QuadrupleIntegerConst) inlinedStartBlock.instructions.stream().
                filter(p -> p.getInstruction() == IRInstruction.ENTER).findFirst().orElseThrow().getArg1()).getValue();

        currentSequence.entryBlock.instructions.stream().
                filter(p -> p.getInstruction() == IRInstruction.ENTER).findFirst().orElseThrow().
                setArg1(new QuadrupleIntegerConst(holder + inlined));
    }

    private void fixCallParameters(BasicBlock startFrom, List<Quadruple> callParameters, Quadruple callInstruction) {
        Obj functionObj = ((QuadrupleObjVar) callInstruction.getArg1()).getObj();

        List<Quadruple> storeInstructions = new ArrayList<>();

        int i = 0;
        for (Obj params : functionObj.getLocalSymbols()) {
            if (!params.parameter)
                continue;

            Quadruple store = new Quadruple(IRInstruction.STORE);

            store.setArg1(callParameters.get(i++).getArg1());

            QuadrupleObjVar objInlined = (QuadrupleObjVar) (new QuadrupleObjVar(params)).makeClone();
            objInlined.getObj().parameter = false;
            store.setResult(objInlined);

            storeInstructions.add(store);

            startFrom.allVariables.add(objInlined.getObj());
            params.inlined = true;
        }

        startFrom.instructions.removeAll(callParameters);
        startFrom.instructions.addAll(startFrom.instructions.size() - 1, storeInstructions);
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

        // deleting call instruction
        startFrom.instructions.remove(callInstruction);

        // fix return statement if function is not void
        Quadruple returnInstruction = inlinedEndBlock.instructions.stream().
                filter(p -> p.getInstruction() == IRInstruction.RETURN).
                findFirst().orElse(null);
        if (returnInstruction != null) {
            Quadruple store = new Quadruple(IRInstruction.STORE);

            store.setArg1(returnInstruction.getArg1());
            Obj resultTmp = ((QuadrupleObjVar) callInstruction.getResult()).getObj();
            resultTmp.tempVar = false;
            store.setResult(new QuadrupleObjVar(resultTmp));

            inlinedEndBlock.instructions.remove(returnInstruction);
            inlinedEndBlock.instructions.add(store);
        }
    }

    public void finalizePass() {
        Config.inlinedAddressOffset = 0;
        if (cnt > 0) {
            currentSequence.dominanceAnalyzer = new DominanceAnalyzer(currentSequence);

            debugVariableAddresses();
        }
    }

    private void debugVariableAddresses() {
        Set<Obj> objs = new HashSet<>();

        for (BasicBlock b : currentSequence.basicBlocks)
            objs.addAll(b.allVariables.stream().filter(p -> p.getKind() == Obj.Var).collect(Collectors.toSet()));

        int oldAllocationValue = 0;//((QuadrupleIntegerConst) currentSequence.entryBlock.instructions.get(1).getArg1()).getValue();
        int lastSize = IROptimizer.giveAddressToAll(objs, oldAllocationValue);
        currentSequence.entryBlock.instructions.get(1).setArg1(new QuadrupleIntegerConst(SystemV_ABI.alignTo16(lastSize)));

        objs.stream().sorted(Comparator.comparingInt(Obj::getAdr)).
                forEach(p -> System.out.println(p.getName() + ": " + p.getAdr()));
        System.out.println();
    }
}
