// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class BooleanConst extends ConstValue {

    private Boolean constBoolValue;

    public BooleanConst(Boolean constBoolValue) {
        this.constBoolValue = constBoolValue;
    }

    public Boolean getConstBoolValue() {
        return constBoolValue;
    }

    public void setConstBoolValue(Boolean constBoolValue) {
        this.constBoolValue = constBoolValue;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("BooleanConst(\n");

        buffer.append(" " + tab + constBoolValue);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [BooleanConst]");
        return buffer.toString();
    }
}
