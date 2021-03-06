package cvetkovic.ir.expression;

import rs.etf.pp1.symboltable.concepts.Obj;

public class ExpressionNode {
    protected static int uniqueId = 0;

    protected int id;
    protected Obj destinationVariable;              // used if inner node

    protected ExpressionNodeOperation operation;
    protected Obj variable;                         // used if leaf node
    protected ExpressionNode leftChild;
    protected ExpressionNode rightChild;

    public ExpressionNode(Obj variable) {
        this.operation = ExpressionNodeOperation.VARIABLE;
        this.variable = variable;
    }

    public ExpressionNode(ExpressionNodeOperation operation, ExpressionNode leftChild) {
        this.operation = operation;
        this.leftChild = leftChild;
    }

    public ExpressionNode(ExpressionNodeOperation operation, ExpressionNode leftChild, ExpressionNode rightChild) {
        this.operation = operation;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public ExpressionNodeOperation getOperation() {
        return operation;
    }

    public Obj getVariable() {
        return variable;
    }

    public ExpressionNode getLeftChild() {
        return leftChild;
    }

    public ExpressionNode getRightChild() {
        return rightChild;
    }

    public boolean isLeaf() {
        return !isUnaryOperator() && !isBinaryOperator();
    }

    public boolean isInnerNode() {
        return !isLeaf();
    }

    public boolean isUnaryOperator() {
        return (operation != null) && (leftChild != null) && (rightChild == null);
    }

    public boolean isBinaryOperator() {
        return (operation != null) && (leftChild != null) && (rightChild != null);
    }

    @Override
    public int hashCode() {
        if (isLeaf())
            return 0;
        else if (isUnaryOperator())
            return 1;
        else
            return 2;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExpressionNode))
            throw new RuntimeException("Invalid type to do equality comparison.");

        ExpressionNode node = (ExpressionNode) obj;

        return operation == node.operation &&
                variable == node.variable &&
                leftChild == node.leftChild &&
                rightChild == node.rightChild;
    }

    public static void resetId() {
        uniqueId = 0;
    }

    public int assignId() {
        return id = uniqueId++;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("[");
        builder.append(id);

        if (isLeaf()) {
            builder.append(", ");
            if (variable.getKind() != Obj.Con) {
                if (variable.getName().startsWith("ArrayAccess_"))
                    builder.append(variable.getName().substring(12));
                else
                    builder.append(variable.getName());
            }
            else
                builder.append(variable.getAdr());

            builder.append("]");
        }
        else if (isUnaryOperator()) {
            builder.append(", uminus, ");
            builder.append(leftChild.id);
            builder.append("]");
        }
        else if (isBinaryOperator()) {
            switch (operation) {
                case ADDITION:
                    builder.append(", +");
                    break;
                case SUBTRACTION:
                    builder.append(", -");
                    break;
                case MULTIPLICATION:
                    builder.append(", *");
                    break;
                case DIVISION:
                    builder.append(", /");
                    break;
                case MODULO:
                    builder.append(", %");
                    break;
                case ASSIGNMENT:
                    builder.append(", =");
                    break;
                case ARRAY_LOAD:
                    builder.append(", =[]");
                    break;
                case ARRAY_STORE:
                    builder.append(", []=");
                    break;
            }

            builder.append(", ");
            builder.append(leftChild.id);
            builder.append(", ");
            builder.append(rightChild.id);
            builder.append("]");
        }

        return builder.toString();
    }

    public Obj getDestinationVariable() {
        return destinationVariable;
    }

    public void setDestinationVariable(Obj destinationVariable) {
        this.destinationVariable = destinationVariable;
    }

    public Obj getObj()
    {
        if (isLeaf())
            return variable;
        else if (isInnerNode())
            return destinationVariable;
        else
            throw new RuntimeException("This method cannot be outside of leaf or inner node.");
    }
}
