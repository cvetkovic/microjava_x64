// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class DataType extends Type {

    private String typeIdent;

    public DataType(String typeIdent) {
        this.typeIdent = typeIdent;
    }

    public String getTypeIdent() {
        return typeIdent;
    }

    public void setTypeIdent(String typeIdent) {
        this.typeIdent = typeIdent;
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
        buffer.append("DataType(\n");

        buffer.append(" " + tab + typeIdent);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DataType]");
        return buffer.toString();
    }
}
