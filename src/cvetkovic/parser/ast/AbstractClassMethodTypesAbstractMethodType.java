// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class AbstractClassMethodTypesAbstractMethodType extends AbstractClassMethodTypesAllowed {

    private AbstractMethodDecl AbstractMethodDecl;

    public AbstractClassMethodTypesAbstractMethodType(AbstractMethodDecl AbstractMethodDecl) {
        this.AbstractMethodDecl = AbstractMethodDecl;
        if (AbstractMethodDecl != null) AbstractMethodDecl.setParent(this);
    }

    public AbstractMethodDecl getAbstractMethodDecl() {
        return AbstractMethodDecl;
    }

    public void setAbstractMethodDecl(AbstractMethodDecl AbstractMethodDecl) {
        this.AbstractMethodDecl = AbstractMethodDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (AbstractMethodDecl != null) AbstractMethodDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (AbstractMethodDecl != null) AbstractMethodDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (AbstractMethodDecl != null) AbstractMethodDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("AbstractClassMethodTypesAbstractMethodType(\n");

        if (AbstractMethodDecl != null)
            buffer.append(AbstractMethodDecl.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [AbstractClassMethodTypesAbstractMethodType]");
        return buffer.toString();
    }
}
