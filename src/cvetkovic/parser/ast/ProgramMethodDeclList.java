// generated with ast extension for cup
// version 0.8
// 27/2/2020 19:41:35


package cvetkovic.parser.ast;

public class ProgramMethodDeclList extends ProgramMethodsDeclList {

    private ProgramMethodsDeclList ProgramMethodsDeclList;
    private MethodDecl MethodDecl;

    public ProgramMethodDeclList (ProgramMethodsDeclList ProgramMethodsDeclList, MethodDecl MethodDecl) {
        this.ProgramMethodsDeclList=ProgramMethodsDeclList;
        if(ProgramMethodsDeclList!=null) ProgramMethodsDeclList.setParent(this);
        this.MethodDecl=MethodDecl;
        if(MethodDecl!=null) MethodDecl.setParent(this);
    }

    public ProgramMethodsDeclList getProgramMethodsDeclList() {
        return ProgramMethodsDeclList;
    }

    public void setProgramMethodsDeclList(ProgramMethodsDeclList ProgramMethodsDeclList) {
        this.ProgramMethodsDeclList=ProgramMethodsDeclList;
    }

    public MethodDecl getMethodDecl() {
        return MethodDecl;
    }

    public void setMethodDecl(MethodDecl MethodDecl) {
        this.MethodDecl=MethodDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ProgramMethodsDeclList!=null) ProgramMethodsDeclList.accept(visitor);
        if(MethodDecl!=null) MethodDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ProgramMethodsDeclList!=null) ProgramMethodsDeclList.traverseTopDown(visitor);
        if(MethodDecl!=null) MethodDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ProgramMethodsDeclList!=null) ProgramMethodsDeclList.traverseBottomUp(visitor);
        if(MethodDecl!=null) MethodDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ProgramMethodDeclList(\n");

        if(ProgramMethodsDeclList!=null)
            buffer.append(ProgramMethodsDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MethodDecl!=null)
            buffer.append(MethodDecl.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ProgramMethodDeclList]");
        return buffer.toString();
    }
}
