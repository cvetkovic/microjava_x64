// generated with ast extension for cup
// version 0.8
// 4/6/2020 14:32:26


package cvetkovic.parser.ast;

public class NoAbstractClassMultipleMethodTypes extends AbstractClassMultipleMethodTypes {

    public NoAbstractClassMultipleMethodTypes () {
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
        buffer.append("NoAbstractClassMultipleMethodTypes(\n");

        buffer.append(tab);
        buffer.append(") [NoAbstractClassMultipleMethodTypes]");
        return buffer.toString();
    }
}
