// generated with ast extension for cup
// version 0.8
// 25/2/2020 20:23:2


package cvetkovic.parser.ast;

public class NoFormParamArray extends FormParamArray {

    public NoFormParamArray () {
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
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("NoFormParamArray(\n");

        buffer.append(tab);
        buffer.append(") [NoFormParamArray]");
        return buffer.toString();
    }
}
