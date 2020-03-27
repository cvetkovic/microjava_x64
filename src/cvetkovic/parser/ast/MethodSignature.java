// generated with ast extension for cup
// version 0.8
// 27/2/2020 19:41:35


package cvetkovic.parser.ast;

public class MethodSignature implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    public rs.etf.pp1.symboltable.concepts.Obj obj = null;

    private MethodDeclReturnType MethodDeclReturnType;
    private MethodName MethodName;
    private MethodParameters MethodParameters;

    public MethodSignature (MethodDeclReturnType MethodDeclReturnType, MethodName MethodName, MethodParameters MethodParameters) {
        this.MethodDeclReturnType=MethodDeclReturnType;
        if(MethodDeclReturnType!=null) MethodDeclReturnType.setParent(this);
        this.MethodName=MethodName;
        if(MethodName!=null) MethodName.setParent(this);
        this.MethodParameters=MethodParameters;
        if(MethodParameters!=null) MethodParameters.setParent(this);
    }

    public MethodDeclReturnType getMethodDeclReturnType() {
        return MethodDeclReturnType;
    }

    public void setMethodDeclReturnType(MethodDeclReturnType MethodDeclReturnType) {
        this.MethodDeclReturnType=MethodDeclReturnType;
    }

    public MethodName getMethodName() {
        return MethodName;
    }

    public void setMethodName(MethodName MethodName) {
        this.MethodName=MethodName;
    }

    public MethodParameters getMethodParameters() {
        return MethodParameters;
    }

    public void setMethodParameters(MethodParameters MethodParameters) {
        this.MethodParameters=MethodParameters;
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
        if(MethodDeclReturnType!=null) MethodDeclReturnType.accept(visitor);
        if(MethodName!=null) MethodName.accept(visitor);
        if(MethodParameters!=null) MethodParameters.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MethodDeclReturnType!=null) MethodDeclReturnType.traverseTopDown(visitor);
        if(MethodName!=null) MethodName.traverseTopDown(visitor);
        if(MethodParameters!=null) MethodParameters.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MethodDeclReturnType!=null) MethodDeclReturnType.traverseBottomUp(visitor);
        if(MethodName!=null) MethodName.traverseBottomUp(visitor);
        if(MethodParameters!=null) MethodParameters.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MethodSignature(\n");

        if(MethodDeclReturnType!=null)
            buffer.append(MethodDeclReturnType.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MethodName!=null)
            buffer.append(MethodName.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MethodParameters!=null)
            buffer.append(MethodParameters.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MethodSignature]");
        return buffer.toString();
    }
}
