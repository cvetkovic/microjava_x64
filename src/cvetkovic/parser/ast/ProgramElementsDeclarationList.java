// generated with ast extension for cup
// version 0.8
// 25/2/2020 20:23:2


package cvetkovic.parser.ast;

public class ProgramElementsDeclarationList extends ProgramElementsDeclList {

    private ProgramElementsDeclList ProgramElementsDeclList;
    private ProgramElementsDeclListElement ProgramElementsDeclListElement;

    public ProgramElementsDeclarationList (ProgramElementsDeclList ProgramElementsDeclList, ProgramElementsDeclListElement ProgramElementsDeclListElement) {
        this.ProgramElementsDeclList=ProgramElementsDeclList;
        if(ProgramElementsDeclList!=null) ProgramElementsDeclList.setParent(this);
        this.ProgramElementsDeclListElement=ProgramElementsDeclListElement;
        if(ProgramElementsDeclListElement!=null) ProgramElementsDeclListElement.setParent(this);
    }

    public ProgramElementsDeclList getProgramElementsDeclList() {
        return ProgramElementsDeclList;
    }

    public void setProgramElementsDeclList(ProgramElementsDeclList ProgramElementsDeclList) {
        this.ProgramElementsDeclList=ProgramElementsDeclList;
    }

    public ProgramElementsDeclListElement getProgramElementsDeclListElement() {
        return ProgramElementsDeclListElement;
    }

    public void setProgramElementsDeclListElement(ProgramElementsDeclListElement ProgramElementsDeclListElement) {
        this.ProgramElementsDeclListElement=ProgramElementsDeclListElement;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ProgramElementsDeclList!=null) ProgramElementsDeclList.accept(visitor);
        if(ProgramElementsDeclListElement!=null) ProgramElementsDeclListElement.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ProgramElementsDeclList!=null) ProgramElementsDeclList.traverseTopDown(visitor);
        if(ProgramElementsDeclListElement!=null) ProgramElementsDeclListElement.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ProgramElementsDeclList!=null) ProgramElementsDeclList.traverseBottomUp(visitor);
        if(ProgramElementsDeclListElement!=null) ProgramElementsDeclListElement.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ProgramElementsDeclarationList(\n");

        if(ProgramElementsDeclList!=null)
            buffer.append(ProgramElementsDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ProgramElementsDeclListElement!=null)
            buffer.append(ProgramElementsDeclListElement.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ProgramElementsDeclarationList]");
        return buffer.toString();
    }
}
