// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class ClassVarListt extends ClassVarList {

    private ClassVarList ClassVarList;
    private VarDecl VarDecl;

    public ClassVarListt(ClassVarList ClassVarList, VarDecl VarDecl) {
        this.ClassVarList = ClassVarList;
        if (ClassVarList != null) ClassVarList.setParent(this);
        this.VarDecl = VarDecl;
        if (VarDecl != null) VarDecl.setParent(this);
    }

    public ClassVarList getClassVarList() {
        return ClassVarList;
    }

    public void setClassVarList(ClassVarList ClassVarList) {
        this.ClassVarList = ClassVarList;
    }

    public VarDecl getVarDecl() {
        return VarDecl;
    }

    public void setVarDecl(VarDecl VarDecl) {
        this.VarDecl = VarDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (ClassVarList != null) ClassVarList.accept(visitor);
        if (VarDecl != null) VarDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (ClassVarList != null) ClassVarList.traverseTopDown(visitor);
        if (VarDecl != null) VarDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (ClassVarList != null) ClassVarList.traverseBottomUp(visitor);
        if (VarDecl != null) VarDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("ClassVarListt(\n");

        if (ClassVarList != null)
            buffer.append(ClassVarList.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (VarDecl != null)
            buffer.append(VarDecl.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ClassVarListt]");
        return buffer.toString();
    }
}
