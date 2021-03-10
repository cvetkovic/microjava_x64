package cvetkovic.ir.quadruple.arguments;

public class QuadruplePTR extends QuadrupleVariable {
    @Override
    public String toString() {
        return "PTR";
    }

    @Override
    public QuadrupleVariable makeClone() {
        return new QuadruplePTR();
    }
}
