// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class VarDeclarationRoot extends VarDecl {

    private Type Type;
    private MultipleVarDeclaration MultipleVarDeclaration;

    public VarDeclarationRoot(Type Type, MultipleVarDeclaration MultipleVarDeclaration) {
        this.Type = Type;
        if (Type != null) Type.setParent(this);
        this.MultipleVarDeclaration = MultipleVarDeclaration;
        if (MultipleVarDeclaration != null) MultipleVarDeclaration.setParent(this);
    }

    public Type getType() {
        return Type;
    }

    public void setType(Type Type) {
        this.Type = Type;
    }

    public MultipleVarDeclaration getMultipleVarDeclaration() {
        return MultipleVarDeclaration;
    }

    public void setMultipleVarDeclaration(MultipleVarDeclaration MultipleVarDeclaration) {
        this.MultipleVarDeclaration = MultipleVarDeclaration;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (Type != null) Type.accept(visitor);
        if (MultipleVarDeclaration != null) MultipleVarDeclaration.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (Type != null) Type.traverseTopDown(visitor);
        if (MultipleVarDeclaration != null) MultipleVarDeclaration.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (Type != null) Type.traverseBottomUp(visitor);
        if (MultipleVarDeclaration != null) MultipleVarDeclaration.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("VarDeclarationRoot(\n");

        if (Type != null)
            buffer.append(Type.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (MultipleVarDeclaration != null)
            buffer.append(MultipleVarDeclaration.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [VarDeclarationRoot]");
        return buffer.toString();
    }
}
