// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class SingleVariableDeclaration extends SingleVarDeclaration {

    private String variableName;
    private SingleVarArray SingleVarArray;

    public SingleVariableDeclaration(String variableName, SingleVarArray SingleVarArray) {
        this.variableName = variableName;
        this.SingleVarArray = SingleVarArray;
        if (SingleVarArray != null) SingleVarArray.setParent(this);
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public SingleVarArray getSingleVarArray() {
        return SingleVarArray;
    }

    public void setSingleVarArray(SingleVarArray SingleVarArray) {
        this.SingleVarArray = SingleVarArray;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (SingleVarArray != null) SingleVarArray.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (SingleVarArray != null) SingleVarArray.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (SingleVarArray != null) SingleVarArray.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("SingleVariableDeclaration(\n");

        buffer.append(" " + tab + variableName);
        buffer.append("\n");

        if (SingleVarArray != null)
            buffer.append(SingleVarArray.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [SingleVariableDeclaration]");
        return buffer.toString();
    }
}
