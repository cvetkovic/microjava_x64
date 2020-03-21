// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class FormPars implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    private SingleFormalParameter SingleFormalParameter;
    private MultipleFormalParameter MultipleFormalParameter;

    public FormPars(SingleFormalParameter SingleFormalParameter, MultipleFormalParameter MultipleFormalParameter) {
        this.SingleFormalParameter = SingleFormalParameter;
        if (SingleFormalParameter != null) SingleFormalParameter.setParent(this);
        this.MultipleFormalParameter = MultipleFormalParameter;
        if (MultipleFormalParameter != null) MultipleFormalParameter.setParent(this);
    }

    public SingleFormalParameter getSingleFormalParameter() {
        return SingleFormalParameter;
    }

    public void setSingleFormalParameter(SingleFormalParameter SingleFormalParameter) {
        this.SingleFormalParameter = SingleFormalParameter;
    }

    public MultipleFormalParameter getMultipleFormalParameter() {
        return MultipleFormalParameter;
    }

    public void setMultipleFormalParameter(MultipleFormalParameter MultipleFormalParameter) {
        this.MultipleFormalParameter = MultipleFormalParameter;
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
        if (SingleFormalParameter != null) SingleFormalParameter.accept(visitor);
        if (MultipleFormalParameter != null) MultipleFormalParameter.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (SingleFormalParameter != null) SingleFormalParameter.traverseTopDown(visitor);
        if (MultipleFormalParameter != null) MultipleFormalParameter.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (SingleFormalParameter != null) SingleFormalParameter.traverseBottomUp(visitor);
        if (MultipleFormalParameter != null) MultipleFormalParameter.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("FormPars(\n");

        if (SingleFormalParameter != null)
            buffer.append(SingleFormalParameter.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (MultipleFormalParameter != null)
            buffer.append(MultipleFormalParameter.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FormPars]");
        return buffer.toString();
    }
}
