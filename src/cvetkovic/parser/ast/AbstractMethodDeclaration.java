// generated with ast extension for cup
// version 0.8
// 20/2/2020 20:30:18


package cvetkovic.parser.ast;

public class AbstractMethodDeclaration extends AbstractMethodDecl {

    private AbstractMethodReturnType AbstractMethodReturnType;
    private AbstractMethodName AbstractMethodName;
    private AbstractMethodParameters AbstractMethodParameters;

    public AbstractMethodDeclaration(AbstractMethodReturnType AbstractMethodReturnType, AbstractMethodName AbstractMethodName, AbstractMethodParameters AbstractMethodParameters) {
        this.AbstractMethodReturnType = AbstractMethodReturnType;
        if (AbstractMethodReturnType != null) AbstractMethodReturnType.setParent(this);
        this.AbstractMethodName = AbstractMethodName;
        if (AbstractMethodName != null) AbstractMethodName.setParent(this);
        this.AbstractMethodParameters = AbstractMethodParameters;
        if (AbstractMethodParameters != null) AbstractMethodParameters.setParent(this);
    }

    public AbstractMethodReturnType getAbstractMethodReturnType() {
        return AbstractMethodReturnType;
    }

    public void setAbstractMethodReturnType(AbstractMethodReturnType AbstractMethodReturnType) {
        this.AbstractMethodReturnType = AbstractMethodReturnType;
    }

    public AbstractMethodName getAbstractMethodName() {
        return AbstractMethodName;
    }

    public void setAbstractMethodName(AbstractMethodName AbstractMethodName) {
        this.AbstractMethodName = AbstractMethodName;
    }

    public AbstractMethodParameters getAbstractMethodParameters() {
        return AbstractMethodParameters;
    }

    public void setAbstractMethodParameters(AbstractMethodParameters AbstractMethodParameters) {
        this.AbstractMethodParameters = AbstractMethodParameters;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (AbstractMethodReturnType != null) AbstractMethodReturnType.accept(visitor);
        if (AbstractMethodName != null) AbstractMethodName.accept(visitor);
        if (AbstractMethodParameters != null) AbstractMethodParameters.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (AbstractMethodReturnType != null) AbstractMethodReturnType.traverseTopDown(visitor);
        if (AbstractMethodName != null) AbstractMethodName.traverseTopDown(visitor);
        if (AbstractMethodParameters != null) AbstractMethodParameters.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (AbstractMethodReturnType != null) AbstractMethodReturnType.traverseBottomUp(visitor);
        if (AbstractMethodName != null) AbstractMethodName.traverseBottomUp(visitor);
        if (AbstractMethodParameters != null) AbstractMethodParameters.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("AbstractMethodDeclaration(\n");

        if (AbstractMethodReturnType != null)
            buffer.append(AbstractMethodReturnType.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (AbstractMethodName != null)
            buffer.append(AbstractMethodName.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (AbstractMethodParameters != null)
            buffer.append(AbstractMethodParameters.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [AbstractMethodDeclaration]");
        return buffer.toString();
    }
}
