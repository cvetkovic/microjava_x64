// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class AbstractClassVariableList extends AbstractClassVarList {

    private AbstractClassVarList AbstractClassVarList;
    private VarDecl VarDecl;

    public AbstractClassVariableList(AbstractClassVarList AbstractClassVarList, VarDecl VarDecl) {
        this.AbstractClassVarList = AbstractClassVarList;
        if (AbstractClassVarList != null) AbstractClassVarList.setParent(this);
        this.VarDecl = VarDecl;
        if (VarDecl != null) VarDecl.setParent(this);
    }

    public AbstractClassVarList getAbstractClassVarList() {
        return AbstractClassVarList;
    }

    public void setAbstractClassVarList(AbstractClassVarList AbstractClassVarList) {
        this.AbstractClassVarList = AbstractClassVarList;
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
        if (AbstractClassVarList != null) AbstractClassVarList.accept(visitor);
        if (VarDecl != null) VarDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (AbstractClassVarList != null) AbstractClassVarList.traverseTopDown(visitor);
        if (VarDecl != null) VarDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (AbstractClassVarList != null) AbstractClassVarList.traverseBottomUp(visitor);
        if (VarDecl != null) VarDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("AbstractClassVariableList(\n");

        if (AbstractClassVarList != null)
            buffer.append(AbstractClassVarList.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (VarDecl != null)
            buffer.append(VarDecl.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [AbstractClassVariableList]");
        return buffer.toString();
    }
}
