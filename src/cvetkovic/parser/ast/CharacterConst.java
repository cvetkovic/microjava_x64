// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class CharacterConst extends ConstValue {

    private Character constCharValue;

    public CharacterConst(Character constCharValue) {
        this.constCharValue = constCharValue;
    }

    public Character getConstCharValue() {
        return constCharValue;
    }

    public void setConstCharValue(Character constCharValue) {
        this.constCharValue = constCharValue;
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
        buffer.append("CharacterConst(\n");

        buffer.append(" " + tab + constCharValue);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [CharacterConst]");
        return buffer.toString();
    }
}
