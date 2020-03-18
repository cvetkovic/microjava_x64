// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class Program implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    public rs.etf.pp1.symboltable.concepts.Obj obj = null;

    private ProgramName ProgramName;
    private ProgramElementsDeclList ProgramElementsDeclList;
    private ProgramMethodsDeclList ProgramMethodsDeclList;

    public Program(ProgramName ProgramName, ProgramElementsDeclList ProgramElementsDeclList, ProgramMethodsDeclList ProgramMethodsDeclList) {
        this.ProgramName = ProgramName;
        if (ProgramName != null) ProgramName.setParent(this);
        this.ProgramElementsDeclList = ProgramElementsDeclList;
        if (ProgramElementsDeclList != null) ProgramElementsDeclList.setParent(this);
        this.ProgramMethodsDeclList = ProgramMethodsDeclList;
        if (ProgramMethodsDeclList != null) ProgramMethodsDeclList.setParent(this);
    }

    public ProgramName getProgramName() {
        return ProgramName;
    }

    public void setProgramName(ProgramName ProgramName) {
        this.ProgramName = ProgramName;
    }

    public ProgramElementsDeclList getProgramElementsDeclList() {
        return ProgramElementsDeclList;
    }

    public void setProgramElementsDeclList(ProgramElementsDeclList ProgramElementsDeclList) {
        this.ProgramElementsDeclList = ProgramElementsDeclList;
    }

    public ProgramMethodsDeclList getProgramMethodsDeclList() {
        return ProgramMethodsDeclList;
    }

    public void setProgramMethodsDeclList(ProgramMethodsDeclList ProgramMethodsDeclList) {
        this.ProgramMethodsDeclList = ProgramMethodsDeclList;
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
        if (ProgramName != null) ProgramName.accept(visitor);
        if (ProgramElementsDeclList != null) ProgramElementsDeclList.accept(visitor);
        if (ProgramMethodsDeclList != null) ProgramMethodsDeclList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (ProgramName != null) ProgramName.traverseTopDown(visitor);
        if (ProgramElementsDeclList != null) ProgramElementsDeclList.traverseTopDown(visitor);
        if (ProgramMethodsDeclList != null) ProgramMethodsDeclList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (ProgramName != null) ProgramName.traverseBottomUp(visitor);
        if (ProgramElementsDeclList != null) ProgramElementsDeclList.traverseBottomUp(visitor);
        if (ProgramMethodsDeclList != null) ProgramMethodsDeclList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("Program(\n");

        if (ProgramName != null)
            buffer.append(ProgramName.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (ProgramElementsDeclList != null)
            buffer.append(ProgramElementsDeclList.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (ProgramMethodsDeclList != null)
            buffer.append(ProgramMethodsDeclList.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Program]");
        return buffer.toString();
    }
}
