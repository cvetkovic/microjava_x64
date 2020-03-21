// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class MultipleVariablesDeclaration extends MultipleVarDeclaration {

    private MultipleVarDeclaration MultipleVarDeclaration;
    private SingleVarDeclaration SingleVarDeclaration;

    public MultipleVariablesDeclaration(MultipleVarDeclaration MultipleVarDeclaration, SingleVarDeclaration SingleVarDeclaration) {
        this.MultipleVarDeclaration = MultipleVarDeclaration;
        if (MultipleVarDeclaration != null) MultipleVarDeclaration.setParent(this);
        this.SingleVarDeclaration = SingleVarDeclaration;
        if (SingleVarDeclaration != null) SingleVarDeclaration.setParent(this);
    }

    public MultipleVarDeclaration getMultipleVarDeclaration() {
        return MultipleVarDeclaration;
    }

    public void setMultipleVarDeclaration(MultipleVarDeclaration MultipleVarDeclaration) {
        this.MultipleVarDeclaration = MultipleVarDeclaration;
    }

    public SingleVarDeclaration getSingleVarDeclaration() {
        return SingleVarDeclaration;
    }

    public void setSingleVarDeclaration(SingleVarDeclaration SingleVarDeclaration) {
        this.SingleVarDeclaration = SingleVarDeclaration;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (MultipleVarDeclaration != null) MultipleVarDeclaration.accept(visitor);
        if (SingleVarDeclaration != null) SingleVarDeclaration.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (MultipleVarDeclaration != null) MultipleVarDeclaration.traverseTopDown(visitor);
        if (SingleVarDeclaration != null) SingleVarDeclaration.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (MultipleVarDeclaration != null) MultipleVarDeclaration.traverseBottomUp(visitor);
        if (SingleVarDeclaration != null) SingleVarDeclaration.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("MultipleVariablesDeclaration(\n");

        if (MultipleVarDeclaration != null)
            buffer.append(MultipleVarDeclaration.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (SingleVarDeclaration != null)
            buffer.append(SingleVarDeclaration.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MultipleVariablesDeclaration]");
        return buffer.toString();
    }
}
