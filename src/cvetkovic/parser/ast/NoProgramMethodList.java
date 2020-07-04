// generated with ast extension for cup
// version 0.8
// 4/6/2020 14:32:25


package cvetkovic.parser.ast;

public class NoProgramMethodList extends ProgramMethodsDeclList {

    public NoProgramMethodList () {
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
        buffer.append("NoProgramMethodList(\n");

        buffer.append(tab);
        buffer.append(") [NoProgramMethodList]");
        return buffer.toString();
    }
}
