package cvetkovic.ir.quadruple;

public class QuadrupleIntegerConst extends QuadrupleVariable {
    protected int value;

    public QuadrupleIntegerConst(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
