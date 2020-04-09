package cvetkovic.ir.optimizations.local;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.optimizations.local.structures.SubexpressionNode;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.QuadrupleObjVar;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalValueNumbering implements OptimizerPass {

    protected BasicBlock basicBlock;
    protected Map<Obj, SubexpressionNode> nodes = new HashMap<>();
    protected Map<SubexpressionNode, SubexpressionNode> alreadyExistingNodes = new HashMap<>();
    protected List<SubexpressionNode> hangingRoots = new ArrayList<>();
    protected List<List<Obj>> allAliases = new ArrayList<>();

    public LocalValueNumbering(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;

        makeLeafNodes();
    }

    @Override
    /**
     * Eliminate common subexpressions
     */
    public void optimize() {
        List<Quadruple> toRemove = new ArrayList<>();

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

            if (obj1 != null)
                leftChild = nodes.get(obj1);
            if (obj2 != null)
                rightChild = nodes.get(obj2);

            SubexpressionNode node = new SubexpressionNode();
            node.instruction = instruction.getInstruction();
            node.leftChild = leftChild;
            node.rightChild = rightChild;
            node.aliases.add(resultObj);

            // TODO: delete old aliases

            Obj replaceAlgebraWith = AlgebraicIdentities.simplifyAlgebra(instruction, obj1, obj2, resultObj);

            if (alreadyExistingNodes.containsKey(node) ||
                    (obj1.getKind() == Obj.Con && obj2 != null && obj2.getKind() == Obj.Con) ||
                    (obj1.getKind() == Obj.Con && obj2 == null && instruction.getInstruction() == IRInstruction.NEG) ||
                    replaceAlgebraWith != null) {   // looks for the non-leaf node

                boolean doConstantFolding = false;
                int foldedValue = -1;

                if ((obj1.getKind() == Obj.Con && obj2 != null && obj2.getKind() == Obj.Con) ||
                        (obj1.getKind() == Obj.Con && obj2 == null && instruction.getInstruction() == IRInstruction.NEG)) {
                    // constant folding
                    foldedValue = instruction.getFoldedValue();
                    doConstantFolding = true;
                }
                else if (replaceAlgebraWith != null) {
                    // TODO: delete non existing nodes from DAG
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

                    if (qObj1 == resultObj) {
                        q.setArg1(new QuadrupleObjVar(targetObj));
                        //q.setArg1NextUse(instruction.getResultNextUse());
                    }
                    if (qObj2 == targetObj) {
                        q.setArg2(new QuadrupleObjVar(targetObj));
                        //q.setArg2NextUse(instruction.getResultNextUse());
                    }
                    if (qRes == targetObj) {
                        q.setResult(new QuadrupleObjVar(targetObj));
                        //q.setResultNextUse(instruction.getResultNextUse());
                    }
                }
            }
            else {
                if (instruction.getInstruction() == IRInstruction.STORE) {
                    if (alreadyExistingNodes.containsKey(leftChild)) {
                        // situation STORE non-temp to non-temp
                        alreadyExistingNodes.get(leftChild).aliases.add(resultObj);
                        nodes.put(resultObj, alreadyExistingNodes.get(leftChild));
                    }
                    else {
                        // situation STORE non-temp to non-temp
                        nodes.put(resultObj, leftChild);
                    }
                }
                else {
                    alreadyExistingNodes.put(node, node);
                    nodes.put(resultObj, node);

                    // remove hanging children and set new node to hang
                    if (hangingRoots.contains(leftChild))
                        hangingRoots.remove(leftChild);
                    if (hangingRoots.contains(rightChild))
                        hangingRoots.remove(rightChild);
                    hangingRoots.add(node);
                }
            }
        }

        // remove all necessary instuctions
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
