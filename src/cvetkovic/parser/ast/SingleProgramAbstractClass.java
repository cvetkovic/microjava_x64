// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class SingleProgramAbstractClass extends ProgramElementsDeclListElement {

    private AbstractClassDecl AbstractClassDecl;

    public SingleProgramAbstractClass(AbstractClassDecl AbstractClassDecl) {
        this.AbstractClassDecl = AbstractClassDecl;
        if (AbstractClassDecl != null) AbstractClassDecl.setParent(this);
    }

    public AbstractClassDecl getAbstractClassDecl() {
        return AbstractClassDecl;
    }

    public void setAbstractClassDecl(AbstractClassDecl AbstractClassDecl) {
        this.AbstractClassDecl = AbstractClassDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (AbstractClassDecl != null) AbstractClassDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (AbstractClassDecl != null) AbstractClassDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (AbstractClassDecl != null) AbstractClassDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("SingleProgramAbstractClass(\n");

        if (AbstractClassDecl != null)
            buffer.append(AbstractClassDecl.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [SingleProgramAbstractClass]");
        return buffer.toString();
    }
}
