// generated with ast extension for cup
// version 0.8
// 27/2/2020 19:41:35


package cvetkovic.parser.ast;

public class NoArrayDeclaration extends FactorArrayDecl {

    public NoArrayDeclaration () {
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
        buffer.append("NoArrayDeclaration(\n");

        buffer.append(tab);
        buffer.append(") [NoArrayDeclaration]");
        return buffer.toString();
    }
}
