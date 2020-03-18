// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class NoAbstractClassMethodList extends AbstractClassMethodList {

    public NoAbstractClassMethodList() {
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
        buffer.append("NoAbstractClassMethodList(\n");

        buffer.append(tab);
        buffer.append(") [NoAbstractClassMethodList]");
        return buffer.toString();
    }
}
