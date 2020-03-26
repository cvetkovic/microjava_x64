package cvetkovic.ir.quadruple;

import cvetkovic.ir.IRInstruction;

public class Quadruple {
    protected IRInstruction instruction;
    protected QuadrupleVariable arg1;
    protected QuadrupleVariable arg2;
    protected QuadrupleVariable result;

    public Quadruple(IRInstruction instruction) {
        this.instruction = instruction;
    }

    // TODO: add to setters RuntimeException if that parameter cannot be set because of instruction format specification
    // TODO: add explicit typechecking

    public IRInstruction getInstruction() {
        return instruction;
    }

    public QuadrupleVariable getArg1() {
        return arg1;
    }

    public void setArg1(QuadrupleVariable arg1) {
        if (this.arg1 != null)
            throw new RuntimeException("Quadruple arg1 field is immutable.");

        this.arg1 = arg1;
    }

    public QuadrupleVariable getArg2() {
        return arg2;
    }

    public void setArg2(QuadrupleVariable arg2) {
        if (this.arg2 != null)
            throw new RuntimeException("Quadruple arg1 field is immutable.");

        this.arg2 = arg2;
    }

    public QuadrupleVariable getResult() {
        return result;
    }

    public void setResult(QuadrupleVariable result) {
        if (this.result != null)
            throw new RuntimeException("Quadruple arg1 field is immutable.");

        this.result = result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(instruction);
        if (arg1 != null) {
            builder.append(" ");
            builder.append(arg1);
        }
        if (arg2 != null) {
            builder.append(" ");
            builder.append(arg2);
        }
        if (result != null) {
            builder.append(" ");
            builder.append(result);
        }

        return builder.toString();
    }
}
