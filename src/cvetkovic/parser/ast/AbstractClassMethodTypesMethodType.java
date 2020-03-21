// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class AbstractClassMethodTypesMethodType extends AbstractClassMethodTypesAllowed {

    private MethodDecl MethodDecl;

    public AbstractClassMethodTypesMethodType(MethodDecl MethodDecl) {
        this.MethodDecl = MethodDecl;
        if (MethodDecl != null) MethodDecl.setParent(this);
    }

    public MethodDecl getMethodDecl() {
        return MethodDecl;
    }

    public void setMethodDecl(MethodDecl MethodDecl) {
        this.MethodDecl = MethodDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (MethodDecl != null) MethodDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (MethodDecl != null) MethodDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (MethodDecl != null) MethodDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("AbstractClassMethodTypesMethodType(\n");

        if (MethodDecl != null)
            buffer.append(MethodDecl.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [AbstractClassMethodTypesMethodType]");
        return buffer.toString();
    }
}
