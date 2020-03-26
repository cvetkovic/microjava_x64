package cvetkovic.ir.quadruple;

public class QuadrupleLabel extends QuadrupleVariable {
    private String labelName;

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
}
