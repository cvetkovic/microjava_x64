// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class AdditionalConstantDeclaration extends AdditionalConstDeclaration {

    private AdditionalConstDeclaration AdditionalConstDeclaration;
    private SingleConstDeclaration SingleConstDeclaration;

    public AdditionalConstantDeclaration(AdditionalConstDeclaration AdditionalConstDeclaration, SingleConstDeclaration SingleConstDeclaration) {
        this.AdditionalConstDeclaration = AdditionalConstDeclaration;
        if (AdditionalConstDeclaration != null) AdditionalConstDeclaration.setParent(this);
        this.SingleConstDeclaration = SingleConstDeclaration;
        if (SingleConstDeclaration != null) SingleConstDeclaration.setParent(this);
    }

    public AdditionalConstDeclaration getAdditionalConstDeclaration() {
        return AdditionalConstDeclaration;
    }

    public void setAdditionalConstDeclaration(AdditionalConstDeclaration AdditionalConstDeclaration) {
        this.AdditionalConstDeclaration = AdditionalConstDeclaration;
    }

    public SingleConstDeclaration getSingleConstDeclaration() {
        return SingleConstDeclaration;
    }

    public void setSingleConstDeclaration(SingleConstDeclaration SingleConstDeclaration) {
        this.SingleConstDeclaration = SingleConstDeclaration;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (AdditionalConstDeclaration != null) AdditionalConstDeclaration.accept(visitor);
        if (SingleConstDeclaration != null) SingleConstDeclaration.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (AdditionalConstDeclaration != null) AdditionalConstDeclaration.traverseTopDown(visitor);
        if (SingleConstDeclaration != null) SingleConstDeclaration.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (AdditionalConstDeclaration != null) AdditionalConstDeclaration.traverseBottomUp(visitor);
        if (SingleConstDeclaration != null) SingleConstDeclaration.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("AdditionalConstantDeclaration(\n");

        if (AdditionalConstDeclaration != null)
            buffer.append(AdditionalConstDeclaration.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (SingleConstDeclaration != null)
            buffer.append(SingleConstDeclaration.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [AdditionalConstantDeclaration]");
        return buffer.toString();
    }
}
