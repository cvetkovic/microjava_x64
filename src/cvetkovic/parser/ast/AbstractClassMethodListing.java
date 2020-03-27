// generated with ast extension for cup
// version 0.8
// 27/2/2020 19:41:35


package cvetkovic.parser.ast;

public class AbstractClassMethodListing extends AbstractClassMethodList {

    private AbstractClassMultipleMethodTypes AbstractClassMultipleMethodTypes;

    public AbstractClassMethodListing (AbstractClassMultipleMethodTypes AbstractClassMultipleMethodTypes) {
        this.AbstractClassMultipleMethodTypes=AbstractClassMultipleMethodTypes;
        if(AbstractClassMultipleMethodTypes!=null) AbstractClassMultipleMethodTypes.setParent(this);
    }

    public AbstractClassMultipleMethodTypes getAbstractClassMultipleMethodTypes() {
        return AbstractClassMultipleMethodTypes;
    }

    public void setAbstractClassMultipleMethodTypes(AbstractClassMultipleMethodTypes AbstractClassMultipleMethodTypes) {
        this.AbstractClassMultipleMethodTypes=AbstractClassMultipleMethodTypes;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(AbstractClassMultipleMethodTypes!=null) AbstractClassMultipleMethodTypes.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(AbstractClassMultipleMethodTypes!=null) AbstractClassMultipleMethodTypes.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(AbstractClassMultipleMethodTypes!=null) AbstractClassMultipleMethodTypes.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("AbstractClassMethodListing(\n");

        if(AbstractClassMultipleMethodTypes!=null)
            buffer.append(AbstractClassMultipleMethodTypes.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [AbstractClassMethodListing]");
        return buffer.toString();
    }
}
