// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class AbstractClassDecl implements SyntaxNode {

    public rs.etf.pp1.symboltable.concepts.Obj obj = null;
    private SyntaxNode parent;
    private int line;
    private AbstractClassName AbstractClassName;
    private AbstractExtends AbstractExtends;
    private AbstractClassVarList AbstractClassVarList;
    private AbstractClassMethodList AbstractClassMethodList;

    public AbstractClassDecl(AbstractClassName AbstractClassName, AbstractExtends AbstractExtends, AbstractClassVarList AbstractClassVarList, AbstractClassMethodList AbstractClassMethodList) {
        this.AbstractClassName = AbstractClassName;
        if (AbstractClassName != null) AbstractClassName.setParent(this);
        this.AbstractExtends = AbstractExtends;
        if (AbstractExtends != null) AbstractExtends.setParent(this);
        this.AbstractClassVarList = AbstractClassVarList;
        if (AbstractClassVarList != null) AbstractClassVarList.setParent(this);
        this.AbstractClassMethodList = AbstractClassMethodList;
        if (AbstractClassMethodList != null) AbstractClassMethodList.setParent(this);
    }

    public AbstractClassName getAbstractClassName() {
        return AbstractClassName;
    }

    public void setAbstractClassName(AbstractClassName AbstractClassName) {
        this.AbstractClassName = AbstractClassName;
    }

    public AbstractExtends getAbstractExtends() {
        return AbstractExtends;
    }

    public void setAbstractExtends(AbstractExtends AbstractExtends) {
        this.AbstractExtends = AbstractExtends;
    }

    public AbstractClassVarList getAbstractClassVarList() {
        return AbstractClassVarList;
    }

    public void setAbstractClassVarList(AbstractClassVarList AbstractClassVarList) {
        this.AbstractClassVarList = AbstractClassVarList;
    }

    public AbstractClassMethodList getAbstractClassMethodList() {
        return AbstractClassMethodList;
    }

    public void setAbstractClassMethodList(AbstractClassMethodList AbstractClassMethodList) {
        this.AbstractClassMethodList = AbstractClassMethodList;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent = parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (AbstractClassName != null) AbstractClassName.accept(visitor);
        if (AbstractExtends != null) AbstractExtends.accept(visitor);
        if (AbstractClassVarList != null) AbstractClassVarList.accept(visitor);
        if (AbstractClassMethodList != null) AbstractClassMethodList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (AbstractClassName != null) AbstractClassName.traverseTopDown(visitor);
        if (AbstractExtends != null) AbstractExtends.traverseTopDown(visitor);
        if (AbstractClassVarList != null) AbstractClassVarList.traverseTopDown(visitor);
        if (AbstractClassMethodList != null) AbstractClassMethodList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (AbstractClassName != null) AbstractClassName.traverseBottomUp(visitor);
        if (AbstractExtends != null) AbstractExtends.traverseBottomUp(visitor);
        if (AbstractClassVarList != null) AbstractClassVarList.traverseBottomUp(visitor);
        if (AbstractClassMethodList != null) AbstractClassMethodList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("AbstractClassDecl(\n");

        if (AbstractClassName != null)
            buffer.append(AbstractClassName.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (AbstractExtends != null)
            buffer.append(AbstractExtends.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (AbstractClassVarList != null)
            buffer.append(AbstractClassVarList.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (AbstractClassMethodList != null)
            buffer.append(AbstractClassMethodList.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [AbstractClassDecl]");
        return buffer.toString();
    }
}
