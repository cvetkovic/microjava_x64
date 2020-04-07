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

public class CommonSubexpressionElimination implements OptimizerPass {

    protected BasicBlock basicBlock;
    protected Map<Obj, SubexpressionNode> nodes = new HashMap<>();
    protected Map<SubexpressionNode, SubexpressionNode> alreadyExistingNodes = new HashMap<>();
    protected List<SubexpressionNode> hangingRoots = new ArrayList<>();

    public CommonSubexpressionElimination(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;

        makeLeafNodes();
        makeRootNodes();
    }

    @Override
    /**
     * Eliminate common subexpressions
     */
    public void doOptimization() {

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

    /**
     * Root nodes represent the operation, temporary variables or updates
     * non-temporary variables
     */
    private void makeRootNodes() {
        for (Quadruple instruction : basicBlock.instructions) {
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

            if (alreadyExistingNodes.containsKey(node)) {   // looks for the non-leaf node
                node = alreadyExistingNodes.get(node);
                node.aliases.add(resultObj);
                if (!nodes.containsKey(resultObj))  // added for aliasing in case new not is not created
                    nodes.put(resultObj, node);
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
    }
}
