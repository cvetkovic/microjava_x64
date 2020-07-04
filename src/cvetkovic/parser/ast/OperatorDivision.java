// generated with ast extension for cup
// version 0.8
// 4/6/2020 14:32:27


package cvetkovic.parser.ast;

public class OperatorDivision extends Mulop {

    public OperatorDivision () {
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
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("OperatorDivision(\n");

        buffer.append(tab);
        buffer.append(") [OperatorDivision]");
        return buffer.toString();
    }
}
