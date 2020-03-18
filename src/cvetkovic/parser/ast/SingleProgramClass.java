// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class SingleProgramClass extends ProgramElementsDeclListElement {

    private ClassDecl ClassDecl;

    public SingleProgramClass(ClassDecl ClassDecl) {
        this.ClassDecl = ClassDecl;
        if (ClassDecl != null) ClassDecl.setParent(this);
    }

    public ClassDecl getClassDecl() {
        return ClassDecl;
    }

    public void setClassDecl(ClassDecl ClassDecl) {
        this.ClassDecl = ClassDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (ClassDecl != null) ClassDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (ClassDecl != null) ClassDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (ClassDecl != null) ClassDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("SingleProgramClass(\n");

        if (ClassDecl != null)
            buffer.append(ClassDecl.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [SingleProgramClass]");
        return buffer.toString();
    }
}
