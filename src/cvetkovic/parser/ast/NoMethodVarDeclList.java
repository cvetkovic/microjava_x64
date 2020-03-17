// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class NoMethodVarDeclList extends MethodVarDeclList {

    public NoMethodVarDeclList() {
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
        buffer.append("NoMethodVarDeclList(\n");

        buffer.append(tab);
        buffer.append(") [NoMethodVarDeclList]");
        return buffer.toString();
    }
}
