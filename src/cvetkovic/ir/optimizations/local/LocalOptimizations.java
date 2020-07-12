package cvetkovic.ir.optimizations.local;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.optimizations.local.structures.SubexpressionNode;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalOptimizations implements OptimizerPass {
    protected BasicBlock basicBlock;
    protected Map<Obj, Node> nodes = new HashMap<>();
    protected Map<Node, Node> alreadyExistingNodes = new HashMap<>();

    protected Map<Obj, Integer> SSACount = new HashMap<>();

    public LocalOptimizations(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;

        constructDAG();
    }

    public static class Node {
        public Map<Obj, Integer> aliases = new HashMap<>();

        public IRInstruction instruction;
        public Node leftOperand;
        public Node rightOperand;

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SubexpressionNode))
                throw new RuntimeException("Invalid type to do equality comparison.");

            Node node = (Node) obj;

            if (instruction == node.instruction && (instruction == IRInstruction.ADD || instruction == IRInstruction.MUL))
                return (leftOperand == node.leftOperand && rightOperand == node.rightOperand) ||
                        (leftOperand == node.rightOperand && rightOperand == node.leftOperand);
            else
                return instruction == node.instruction &&
                        leftOperand == node.leftOperand &&
                        rightOperand == node.rightOperand;
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

    private void constructDAG() {
        List<Quadruple> instructions = basicBlock.instructions;

        makeLeafNodes();

        for (int i = 0; i < instructions.size(); i++) {
            Quadruple instruction = instructions.get(i);

            Obj obj1 = null, obj2 = null, resultObj = null;
            if (instruction.getArg1() instanceof QuadrupleObjVar)
                obj1 = ((QuadrupleObjVar) instruction.getArg1()).getObj();
            if (instruction.getArg2() instanceof QuadrupleObjVar)
                obj2 = ((QuadrupleObjVar) instruction.getArg2()).getObj();
            if (instruction.getResult() instanceof QuadrupleObjVar)
                resultObj = ((QuadrupleObjVar) instruction.getResult()).getObj();

            if (obj1 == null && obj2 == null && resultObj == null)
                continue;

            Node node = new Node();
            if (obj1 != null)
                node.leftOperand = nodes.get(obj1);
            if (obj2 != null)
                node.rightOperand = nodes.get(obj2);
            Integer resultCnt = SSACount.computeIfAbsent(resultObj, k -> 0);
            node.aliases.put(resultObj, resultCnt);
        }
    }

    private void makeLeafNodes() {
        for (Obj leafNodes : basicBlock.nonTemporaryVariables) {
            Node node = new Node();
            node.aliases.put(leafNodes, 0);

            nodes.put(leafNodes, node);
        }
    }

    @Override
    public void optimize() {

    }

    @Override
    public void finalizePass() {

    }

}
