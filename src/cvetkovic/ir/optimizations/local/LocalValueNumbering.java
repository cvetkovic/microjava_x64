package cvetkovic.ir.optimizations.local;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.optimizations.local.structures.SubexpressionNode;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadruplePTR;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Optimizer that does constant folding, algebraic identities substitution and common subexpression elimination
 */
public class LocalValueNumbering implements OptimizerPass {

    protected BasicBlock basicBlock;
    protected Map<Obj, SubexpressionNode> nodes = new HashMap<>();
    protected Map<SubexpressionNode, SubexpressionNode> alreadyExistingNodes = new HashMap<>();
    //protected List<SubexpressionNode> hangingRoots = new ArrayList<>()

    // no need for SubexpressionNode list here because only one can node be alive at one time
    protected Map<Obj, SubexpressionNode> arrayAccesses = new HashMap<>();
    protected boolean removeDAG = false;

    public LocalValueNumbering(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;

        makeLeafNodes();
    }

    public void optimize() {
        List<Quadruple> toRemove = new ArrayList<>();

        // DONE ON THE LEVEL OF BASIC BLOCK
        for (int i = 0; i < basicBlock.instructions.size(); i++) {
            Quadruple instruction = basicBlock.instructions.get(i);

            Obj obj1 = null, obj2 = null, resultObj = null;
            SubexpressionNode leftChild = null, rightChild = null;

            if (instruction.getArg1() instanceof QuadrupleObjVar)
                obj1 = ((QuadrupleObjVar) instruction.getArg1()).getObj();
            if (instruction.getArg2() instanceof QuadrupleObjVar)
                obj2 = ((QuadrupleObjVar) instruction.getArg2()).getObj();
            if (instruction.getResult() instanceof QuadrupleObjVar)
                resultObj = ((QuadrupleObjVar) instruction.getResult()).getObj();

            if (obj1 == null && obj2 == null && resultObj == null)
                continue; // instruction doesn't involve any operands
            /*else if (instruction.getInstruction() == IRInstruction.PRINTF)
                continue;
            else if (instruction.getInstruction() == IRInstruction.SCANF)
                continue;
            else if (instruction.getInstruction() == IRInstruction.ASTORE)
                continue;*/

            if (obj1 != null)
                leftChild = nodes.get(obj1);
            if (obj2 != null)
                rightChild = nodes.get(obj2);

            SubexpressionNode node = new SubexpressionNode();
            node.instruction = instruction.getInstruction();
            node.leftChild = leftChild;
            node.rightChild = rightChild;
            node.aliases.add(resultObj);

            boolean nodeAlive = true;
            // checking whether node is alive in case of ALOAD -> [ALSU06] pg. 538
            if (instruction.getInstruction() == IRInstruction.ALOAD) {
                if (alreadyExistingNodes.containsKey(node)) {
                    SubexpressionNode subexpressionNode = alreadyExistingNodes.get(node);
                    if (subexpressionNode.deadNode) {
                        nodeAlive = false;
                        arrayAccesses.remove(obj1);
                    }
                }
                else {
                    arrayAccesses.put(obj1, node);
                }
            }
            else if (instruction.getInstruction() == IRInstruction.ASTORE) {
                // killing old node accesses
                if (arrayAccesses.containsKey(resultObj)) {
                    arrayAccesses.get(resultObj).deadNode = true;
                }
            }
            else if (instruction.getInstruction() == IRInstruction.PARAM) {
                if (obj1.getType().getKind() == Struct.Class)
                    removeDAG = true;
            }
            else if ((instruction.getInstruction() == IRInstruction.STORE &&
                    instruction.getArg2() != null && instruction.getArg2() instanceof QuadruplePTR) ||
                    ((instruction.getInstruction() == IRInstruction.CALL || instruction.getInstruction() == IRInstruction.INVOKE_VIRTUAL) && removeDAG)) {
                nodes.clear();
                alreadyExistingNodes.clear();
                arrayAccesses.clear();
                removeDAG = false;

                makeLeafNodes();

                continue;
            }

            // TODO: delete old aliases

            Obj replaceAlgebraWith = AlgebraicIdentities.simplifyAlgebra(instruction, obj1, obj2, resultObj);

            if ((alreadyExistingNodes.containsKey(node) && nodeAlive) ||
                    (obj1 != null && obj1.getKind() == Obj.Con && obj2 != null && obj2.getKind() == Obj.Con) ||
                    (obj1 != null && obj1.getKind() == Obj.Con && obj2 == null && instruction.getInstruction() == IRInstruction.NEG) ||
                    replaceAlgebraWith != null) {   // looks for the non-leaf node

                boolean doConstantFolding = false;
                int foldedValue = -1;

                if (obj1 != null && ((obj1.getKind() == Obj.Con && obj2 != null && obj2.getKind() == Obj.Con) ||
                        (obj1.getKind() == Obj.Con && obj2 == null && instruction.getInstruction() == IRInstruction.NEG))) {
                    // constant folding
                    foldedValue = instruction.getFoldedValue();
                    doConstantFolding = true;
                }
                else if (replaceAlgebraWith != null) {
                    // TODO: delete non existing nodes from DAG
                    // algebraic identities
                }
                else {
                    node = alreadyExistingNodes.get(node);
                    node.aliases.add(resultObj);
                    if (!nodes.containsKey(resultObj))  // added for aliasing in case new not is not created
                        nodes.put(resultObj, node);
                }

                toRemove.add(instruction);
                for (int j = i + 1; j < basicBlock.instructions.size(); j++) {
                    Quadruple q = basicBlock.instructions.get(j);

                    Obj qObj1 = null, qObj2 = null, qRes = null;

                    if (q.getArg1() instanceof QuadrupleObjVar)
                        qObj1 = ((QuadrupleObjVar) q.getArg1()).getObj();
                    if (q.getArg2() instanceof QuadrupleObjVar)
                        qObj2 = ((QuadrupleObjVar) q.getArg2()).getObj();
                    if (q.getResult() instanceof QuadrupleObjVar)
                        qRes = ((QuadrupleObjVar) q.getResult()).getObj();

                    Obj targetObj = node.aliases.get(0);

                    if (doConstantFolding) {
                        targetObj.changeKind(Obj.Con);
                        targetObj.setAdr(foldedValue);
                    }
                    else if (replaceAlgebraWith != null)
                        targetObj = replaceAlgebraWith;

                    if (qObj1 == resultObj)
                        q.setArg1(new QuadrupleObjVar(targetObj));
                    if (qObj2 == targetObj)
                        q.setArg2(new QuadrupleObjVar(targetObj));
                    if (qRes == targetObj)
                        q.setResult(new QuadrupleObjVar(targetObj));
                    else if (q.getInstruction() == IRInstruction.STORE && q.getArg2() != null && q.getArg2() instanceof QuadruplePTR)
                        q.setResult(new QuadrupleObjVar(nodes.get(resultObj).aliases.get(0)));
                }
            }
            else {
                if (instruction.getInstruction() == IRInstruction.STORE) {
                    // aliasing subexpression nodes
                    if (alreadyExistingNodes.containsKey(leftChild)) {
                        // situation STORE non-temp to non-temp
                        alreadyExistingNodes.get(leftChild).aliases.add(resultObj);
                        nodes.put(resultObj, alreadyExistingNodes.get(leftChild));
                    }
                    else {
                        // situation STORE non-temp to non-temp
                        if (resultObj.tempVar) // equivalent to checking whether arg2 instanceof PTR
                            instruction.setResult(new QuadrupleObjVar(nodes.get(resultObj).aliases.get(0)));
                        else {
                            nodes.put(resultObj, leftChild);
                        }
                    }
                }
                else {
                    alreadyExistingNodes.put(node, node);
                    nodes.put(resultObj, node);

                    // remove hanging children and set new node to hang
                    /*if (hangingRoots.contains(leftChild))
                        hangingRoots.remove(leftChild);
                    if (hangingRoots.contains(rightChild))
                        hangingRoots.remove(rightChild);
                    hangingRoots.add(node);*/
                }
            }
        }

        // remove all necessary instructions
        basicBlock.instructions.removeAll(toRemove);
    }

    /**
     * Liveness needs to be recalculated again
     */
    @Override
    public void finalizePass() {
        basicBlock.doLivenessAnalysis();
    }

    /**
     * Leaf nodes represent non-temporary variables
     */
    private void makeLeafNodes() {
        for (Obj leafNodes : basicBlock.nonTemporaryVariables) {
            SubexpressionNode node = new SubexpressionNode();
            node.aliases.add(leafNodes);

            nodes.put(leafNodes, node);
        }
    }
}
