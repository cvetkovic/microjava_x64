// generated with ast extension for cup
// version 0.8
// 4/6/2020 14:32:26


package cvetkovic.parser.ast;

public class AbstractClassMultipleMethodTypesClass extends AbstractClassMultipleMethodTypes {

    private AbstractClassMultipleMethodTypes AbstractClassMultipleMethodTypes;
    private AbstractClassMethodTypesAllowed AbstractClassMethodTypesAllowed;

    public AbstractClassMultipleMethodTypesClass (AbstractClassMultipleMethodTypes AbstractClassMultipleMethodTypes, AbstractClassMethodTypesAllowed AbstractClassMethodTypesAllowed) {
        this.AbstractClassMultipleMethodTypes=AbstractClassMultipleMethodTypes;
        if(AbstractClassMultipleMethodTypes!=null) AbstractClassMultipleMethodTypes.setParent(this);
        this.AbstractClassMethodTypesAllowed=AbstractClassMethodTypesAllowed;
        if(AbstractClassMethodTypesAllowed!=null) AbstractClassMethodTypesAllowed.setParent(this);
    }

    public AbstractClassMultipleMethodTypes getAbstractClassMultipleMethodTypes() {
        return AbstractClassMultipleMethodTypes;
    }

    public void setAbstractClassMultipleMethodTypes(AbstractClassMultipleMethodTypes AbstractClassMultipleMethodTypes) {
        this.AbstractClassMultipleMethodTypes=AbstractClassMultipleMethodTypes;
    }

    public AbstractClassMethodTypesAllowed getAbstractClassMethodTypesAllowed() {
        return AbstractClassMethodTypesAllowed;
    }

    public void setAbstractClassMethodTypesAllowed(AbstractClassMethodTypesAllowed AbstractClassMethodTypesAllowed) {
        this.AbstractClassMethodTypesAllowed=AbstractClassMethodTypesAllowed;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(AbstractClassMultipleMethodTypes!=null) AbstractClassMultipleMethodTypes.accept(visitor);
        if(AbstractClassMethodTypesAllowed!=null) AbstractClassMethodTypesAllowed.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(AbstractClassMultipleMethodTypes!=null) AbstractClassMultipleMethodTypes.traverseTopDown(visitor);
        if(AbstractClassMethodTypesAllowed!=null) AbstractClassMethodTypesAllowed.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(AbstractClassMultipleMethodTypes!=null) AbstractClassMultipleMethodTypes.traverseBottomUp(visitor);
        if(AbstractClassMethodTypesAllowed!=null) AbstractClassMethodTypesAllowed.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("AbstractClassMultipleMethodTypesClass(\n");

        if(AbstractClassMultipleMethodTypes!=null)
            buffer.append(AbstractClassMultipleMethodTypes.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(AbstractClassMethodTypesAllowed!=null)
            buffer.append(AbstractClassMethodTypesAllowed.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [AbstractClassMultipleMethodTypesClass]");
        return buffer.toString();
    }
}
