// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class AbstractMethodWithReturn extends AbstractMethodReturnType {

    private Type Type;

    public AbstractMethodWithReturn(Type Type) {
        this.Type = Type;
        if (Type != null) Type.setParent(this);
    }

    public Type getType() {
        return Type;
    }

    public void setType(Type Type) {
        this.Type = Type;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (Type != null) Type.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (Type != null) Type.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (Type != null) Type.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("AbstractMethodWithReturn(\n");

        if (Type != null)
            buffer.append(Type.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [AbstractMethodWithReturn]");
        return buffer.toString();
    }
}
