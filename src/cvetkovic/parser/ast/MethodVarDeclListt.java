// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class MethodVarDeclListt extends MethodVarDeclList {

    private MethodVarDeclList MethodVarDeclList;
    private VarDecl VarDecl;

    public MethodVarDeclListt(MethodVarDeclList MethodVarDeclList, VarDecl VarDecl) {
        this.MethodVarDeclList = MethodVarDeclList;
        if (MethodVarDeclList != null) MethodVarDeclList.setParent(this);
        this.VarDecl = VarDecl;
        if (VarDecl != null) VarDecl.setParent(this);
    }

    public MethodVarDeclList getMethodVarDeclList() {
        return MethodVarDeclList;
    }

    public void setMethodVarDeclList(MethodVarDeclList MethodVarDeclList) {
        this.MethodVarDeclList = MethodVarDeclList;
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
        if (MethodVarDeclList != null) MethodVarDeclList.accept(visitor);
        if (VarDecl != null) VarDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (MethodVarDeclList != null) MethodVarDeclList.traverseTopDown(visitor);
        if (VarDecl != null) VarDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (MethodVarDeclList != null) MethodVarDeclList.traverseBottomUp(visitor);
        if (VarDecl != null) VarDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("MethodVarDeclListt(\n");

        if (MethodVarDeclList != null)
            buffer.append(MethodVarDeclList.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (VarDecl != null)
            buffer.append(VarDecl.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MethodVarDeclListt]");
        return buffer.toString();
    }
}
