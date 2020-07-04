// generated with ast extension for cup
// version 0.8
// 4/6/2020 14:32:26


package cvetkovic.parser.ast;

public class MethodDecl implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    public rs.etf.pp1.symboltable.concepts.Obj obj = null;

    private MethodSignature MethodSignature;
    private MethodVarDeclList MethodVarDeclList;
    private MethodStatements MethodStatements;

    public MethodDecl (MethodSignature MethodSignature, MethodVarDeclList MethodVarDeclList, MethodStatements MethodStatements) {
        this.MethodSignature=MethodSignature;
        if(MethodSignature!=null) MethodSignature.setParent(this);
        this.MethodVarDeclList=MethodVarDeclList;
        if(MethodVarDeclList!=null) MethodVarDeclList.setParent(this);
        this.MethodStatements=MethodStatements;
        if(MethodStatements!=null) MethodStatements.setParent(this);
    }

    public MethodSignature getMethodSignature() {
        return MethodSignature;
    }

    public void setMethodSignature(MethodSignature MethodSignature) {
        this.MethodSignature=MethodSignature;
    }

    public MethodVarDeclList getMethodVarDeclList() {
        return MethodVarDeclList;
    }

    public void setMethodVarDeclList(MethodVarDeclList MethodVarDeclList) {
        this.MethodVarDeclList=MethodVarDeclList;
    }

    public MethodStatements getMethodStatements() {
        return MethodStatements;
    }

    public void setMethodStatements(MethodStatements MethodStatements) {
        this.MethodStatements=MethodStatements;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent=parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line=line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(MethodSignature!=null) MethodSignature.accept(visitor);
        if(MethodVarDeclList!=null) MethodVarDeclList.accept(visitor);
        if(MethodStatements!=null) MethodStatements.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MethodSignature!=null) MethodSignature.traverseTopDown(visitor);
        if(MethodVarDeclList!=null) MethodVarDeclList.traverseTopDown(visitor);
        if(MethodStatements!=null) MethodStatements.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MethodSignature!=null) MethodSignature.traverseBottomUp(visitor);
        if(MethodVarDeclList!=null) MethodVarDeclList.traverseBottomUp(visitor);
        if(MethodStatements!=null) MethodStatements.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MethodDecl(\n");

        if(MethodSignature!=null)
            buffer.append(MethodSignature.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MethodVarDeclList!=null)
            buffer.append(MethodVarDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MethodStatements!=null)
            buffer.append(MethodStatements.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MethodDecl]");
        return buffer.toString();
    }
}
