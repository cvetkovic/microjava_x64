package cvetkovic.ir.quadruple.arguments;

public class QuadrupleARR extends QuadrupleVariable {
    @Override
    public String toString() {
        return "ARR";
    }

    @Override
    public QuadrupleVariable makeClone() {
        return new QuadrupleARR();
    }
}
