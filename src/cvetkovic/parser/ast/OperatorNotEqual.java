// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class OperatorNotEqual extends Relop {

    public OperatorNotEqual() {
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("OperatorNotEqual(\n");

        buffer.append(tab);
        buffer.append(") [OperatorNotEqual]");
        return buffer.toString();
    }
}
