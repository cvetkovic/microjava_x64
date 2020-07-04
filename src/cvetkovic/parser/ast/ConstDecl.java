// generated with ast extension for cup
// version 0.8
// 4/6/2020 14:32:25


package cvetkovic.parser.ast;

public class ConstDecl implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    private Type Type;
    private SingleConstDeclaration SingleConstDeclaration;
    private AdditionalConstDeclaration AdditionalConstDeclaration;

    public ConstDecl (Type Type, SingleConstDeclaration SingleConstDeclaration, AdditionalConstDeclaration AdditionalConstDeclaration) {
        this.Type=Type;
        if(Type!=null) Type.setParent(this);
        this.SingleConstDeclaration=SingleConstDeclaration;
        if(SingleConstDeclaration!=null) SingleConstDeclaration.setParent(this);
        this.AdditionalConstDeclaration=AdditionalConstDeclaration;
        if(AdditionalConstDeclaration!=null) AdditionalConstDeclaration.setParent(this);
    }

    public Type getType() {
        return Type;
    }

    public void setType(Type Type) {
        this.Type=Type;
    }

    public SingleConstDeclaration getSingleConstDeclaration() {
        return SingleConstDeclaration;
    }

    public void setSingleConstDeclaration(SingleConstDeclaration SingleConstDeclaration) {
        this.SingleConstDeclaration=SingleConstDeclaration;
    }

    public AdditionalConstDeclaration getAdditionalConstDeclaration() {
        return AdditionalConstDeclaration;
    }

    public void setAdditionalConstDeclaration(AdditionalConstDeclaration AdditionalConstDeclaration) {
        this.AdditionalConstDeclaration=AdditionalConstDeclaration;
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
        if(Type!=null) Type.accept(visitor);
        if(SingleConstDeclaration!=null) SingleConstDeclaration.accept(visitor);
        if(AdditionalConstDeclaration!=null) AdditionalConstDeclaration.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Type!=null) Type.traverseTopDown(visitor);
        if(SingleConstDeclaration!=null) SingleConstDeclaration.traverseTopDown(visitor);
        if(AdditionalConstDeclaration!=null) AdditionalConstDeclaration.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Type!=null) Type.traverseBottomUp(visitor);
        if(SingleConstDeclaration!=null) SingleConstDeclaration.traverseBottomUp(visitor);
        if(AdditionalConstDeclaration!=null) AdditionalConstDeclaration.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ConstDecl(\n");

        if(Type!=null)
            buffer.append(Type.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(SingleConstDeclaration!=null)
            buffer.append(SingleConstDeclaration.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(AdditionalConstDeclaration!=null)
            buffer.append(AdditionalConstDeclaration.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ConstDecl]");
        return buffer.toString();
    }
}
