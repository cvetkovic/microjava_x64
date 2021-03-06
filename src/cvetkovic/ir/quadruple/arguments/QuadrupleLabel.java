package cvetkovic.ir.quadruple.arguments;

public class QuadrupleLabel extends QuadrupleVariable {
    protected final String labelName;

    public QuadrupleLabel(String labelName) {
        this.labelName = labelName;
    }

    public String getLabelName() {
        return labelName;
    }

    @Override
    public String toString() {
        return labelName;
    }

    @Override
    public QuadrupleVariable makeClone() {
        return new QuadrupleLabel(labelName);
    }
}
