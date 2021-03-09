package cvetkovic.ir.optimizations.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ValueNumbering implements OptimizerPass {

    private final CodeSequence sequence;

    public ValueNumbering(CodeSequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public void optimize() {
        LVN();
    }

    @Override
    public void finalizePass() {

    }

    private static class VNElement {
        IRInstruction instruction;

        Obj obj;

        VNElement leftChild;
        VNElement rightChild;

        public boolean equals(Object obj) {
            if (!(obj instanceof VNElement))
                throw new RuntimeException("Invalid type to do equality comparison.");

            VNElement node = (VNElement) obj;

            if (instruction == node.instruction && (instruction == IRInstruction.ADD || instruction == IRInstruction.MUL))
                return (leftChild == node.leftChild && rightChild == node.rightChild) ||
                        (leftChild == node.rightChild && rightChild == node.leftChild);
            else
                return instruction == node.instruction &&
                        leftChild == node.leftChild &&
                        rightChild == node.rightChild;
        }
    }

    private void makeLeafNodes(BasicBlock block,
                               Map<Obj, VNElement> objToVNElement) {

        // do not use BasicBlock.allVariables as it does not contain constants
        Set<Obj> allVars = new HashSet<>();
        for (Quadruple q : block.instructions) {
            if (q.getArg1() instanceof QuadrupleObjVar)
                allVars.add(((QuadrupleObjVar) q.getArg1()).getObj());
            if (q.getArg2() instanceof QuadrupleObjVar)
                allVars.add(((QuadrupleObjVar) q.getArg2()).getObj());
            if (q.getResult() instanceof QuadrupleObjVar)
                allVars.add(((QuadrupleObjVar) q.getResult()).getObj());
        }

        for (Obj obj : allVars) {
            VNElement node = new VNElement();
            node.obj = obj;

            objToVNElement.put(obj, node);
        }
    }

    private void LVN() {
        for (BasicBlock block : sequence.basicBlocks) {
            Map<Obj, VNElement> objToVNElement = new HashMap<>();
            makeLeafNodes(block, objToVNElement);

            Set<Quadruple> toRemove = new HashSet<>();

            for (int i = 0; i < block.instructions.size(); i++) {
                Quadruple instruction = block.instructions.get(i);
                VNElement arg1 = null, arg2 = null;

                // do not process instructions without a result Obj node
                if (instruction.getResult() == null)
                    continue;
                else if (IRInstruction.isJumpInstruction(instruction.getInstruction()))
                    continue;
                switch (instruction.getInstruction()) {
                    case ALOAD:
                    case ASTORE:
                    case MALLOC:
                    case SCANF:
                    case CALL:
                    case INVOKE_VIRTUAL:
                    case GET_PTR:           // arg2 is not type of Obj.Var or Obj.Fld
                        continue;
                }

                if (instruction.getArg1() instanceof QuadrupleObjVar)
                    arg1 = objToVNElement.get(((QuadrupleObjVar) instruction.getArg1()).getObj());
                if (instruction.getArg2() instanceof QuadrupleObjVar)
                    arg2 = objToVNElement.get(((QuadrupleObjVar) instruction.getArg2()).getObj());
                assert arg1 != null || arg2 != null;

                Obj resultObj = ((QuadrupleObjVar) instruction.getResult()).getObj();

                VNElement element = new VNElement();
                element.instruction = instruction.getInstruction();
                element.leftChild = arg1;
                element.rightChild = arg2;
                element.obj = resultObj;

                if (tryConstantFolding(instruction, block))
                    continue;

                VNElement searchAgainstElement = objToVNElement.values().stream().filter(p -> p.equals(element)).findFirst().orElse(null);
                if (searchAgainstElement == null) {
                    objToVNElement.put(resultObj, element);
                } else {
                    Obj replaceWith = searchAgainstElement.obj;
                    for (int j = i + 1; j < block.instructions.size(); j++) {
                        Quadruple q = block.instructions.get(j);

                        // renaming
                        if (q.getArg1() instanceof QuadrupleObjVar && ((QuadrupleObjVar) q.getArg1()).getObj() == resultObj)
                            q.setArg1(new QuadrupleObjVar(replaceWith));
                        if (q.getArg2() instanceof QuadrupleObjVar && ((QuadrupleObjVar) q.getArg2()).getObj() == resultObj)
                            q.setArg2(new QuadrupleObjVar(replaceWith));
                        if (q.getResult() instanceof QuadrupleObjVar && ((QuadrupleObjVar) q.getResult()).getObj() == resultObj)
                            q.setResult(new QuadrupleObjVar(replaceWith));
                    }

                    if (instruction.getInstruction() != IRInstruction.STORE)
                        toRemove.add(instruction);
                }
            }

            block.instructions.removeAll(toRemove);
        }
    }

    //////////////////////////////////////
    // CONSTANT FOLDING
    //////////////////////////////////////

    private Integer getFoldedValue(IRInstruction instruction, Obj obj1, Obj obj2) {
        switch (instruction) {
            case ADD:
                return obj1.getAdr() + obj2.getAdr();
            case SUB:
                return obj1.getAdr() - obj2.getAdr();
            case MUL:
                return obj1.getAdr() * obj2.getAdr();
            case DIV:
                if (obj2.getAdr() == 0)
                    throw new RuntimeException("Code contains division by zero.");
                else
                    return obj1.getAdr() / obj2.getAdr();
            case REM:
                return obj1.getAdr() % obj2.getAdr();
            case NEG:
                return -obj1.getAdr();

            default:
                return null;
        }
    }

    private boolean tryConstantFolding(Quadruple q, BasicBlock block) {
        if (!(q.getArg1() instanceof QuadrupleObjVar && q.getArg2() instanceof QuadrupleObjVar))
            return false;

        Obj obj1 = ((QuadrupleObjVar) q.getArg1()).getObj();
        Obj obj2 = ((QuadrupleObjVar) q.getArg2()).getObj();

        if (obj1.getKind() != Obj.Con || obj2.getKind() != Obj.Con)
            return false;

        Integer foldedValue = getFoldedValue(q.getInstruction(), obj1, obj2);
        if (foldedValue == null)
            return false;
        else {
            block.instructions.replaceAll(quadruple -> {
                if (quadruple != q)
                    return quadruple;
                else {
                    Quadruple newStore = new Quadruple(IRInstruction.STORE);

                    Obj newConst = new Obj(Obj.Con, "const", obj1.getType());
                    newConst.setAdr(foldedValue);

                    newStore.setArg1(new QuadrupleObjVar(newConst));
                    newStore.setResult(q.getResult());

                    return newStore;
                }
            });

            return true;
        }
    }

    /*private boolean isPhiMeaningless(Quadruple phiInstruction) {
        return false;
    }

    private void DVNT(DominanceAnalyzer.DominatorTreeNode dominatorTreeNode) {
        BasicBlock block = dominatorTreeNode.basicBlock;

        // entering new scope
        scopeStack.push(new Scope());

        // processing phi nodes
        List<Quadruple> phiInstructions = block.instructions.stream().
                filter(p -> p.getInstruction() == IRInstruction.STORE_PHI).collect(Collectors.toList());
        for (Quadruple phiInstruction : phiInstructions) {

        }

        for (Quadruple instruction : dominatorTreeNode.basicBlock.instructions) {
            overwriteOperands(instruction);


        }

        for (DominanceAnalyzer.DominatorTreeNode child : dominatorTreeNode.children)
            DVNT(child);

        // leave the scope
        scopeStack.pop();
    }

    private void overwriteOperands(Quadruple instruction) {
        if (instruction.getArg1() instanceof QuadrupleObjVar) {

        }
    }

    private Stack<Scope> scopeStack = new Stack<>();

    private static class Scope {
        Map<Obj, Integer> VN = new HashMap<>();
    }*/
}
