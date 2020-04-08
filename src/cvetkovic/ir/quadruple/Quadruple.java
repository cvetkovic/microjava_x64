package cvetkovic.ir.quadruple;

import cvetkovic.ir.IRInstruction;
import cvetkovic.misc.Config;

public class Quadruple {
    public enum NextUseState {
        UNKNOWN(0),

        DEAD(1),
        ALIVE(2);

        private int state;

        NextUseState(int state) {
            this.state = state;
        }

        @Override
        public String toString() {
            switch (state) {
                case 0:
                    return "(N/A)";
                case 1:
                    return "(D)";
                case 2:
                    return "(A)";

                default:
                    throw new RuntimeException("Unknown next use state in quadruple.");
            }
        }
    }

    protected IRInstruction instruction;
    protected QuadrupleVariable arg1;
    protected QuadrupleVariable arg2;
    protected QuadrupleVariable result;

    protected NextUseState arg1NextUse = NextUseState.UNKNOWN;
    protected NextUseState arg2NextUse = NextUseState.UNKNOWN;
    protected NextUseState resultNextUse = NextUseState.UNKNOWN;

    public Quadruple(IRInstruction instruction) {
        this.instruction = instruction;
    }

    public Quadruple(IRInstruction instruction, QuadrupleVariable arg1, QuadrupleVariable arg2) {
        this.instruction = instruction;
        this.arg1 = arg1;
        this.arg2 = arg2;
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
        this.arg1 = arg1;
    }

    public QuadrupleVariable getArg2() {
        return arg2;
    }

    public void setArg2(QuadrupleVariable arg2) {
        this.arg2 = arg2;
    }

    public QuadrupleVariable getResult() {
        return result;
    }

    public void setResult(QuadrupleVariable result) {
        this.result = result;
    }

    public NextUseState getArg1NextUse() {
        return arg1NextUse;
    }

    public void setArg1NextUse(NextUseState arg1NextUse) {
        this.arg1NextUse = arg1NextUse;
    }

    public NextUseState getArg2NextUse() {
        return arg2NextUse;
    }

    public void setArg2NextUse(NextUseState arg2NextUse) {
        this.arg2NextUse = arg2NextUse;
    }

    public NextUseState getResultNextUse() {
        return resultNextUse;
    }

    public void setResultNextUse(NextUseState resultNextUse) {
        this.resultNextUse = resultNextUse;
    }

    @Override
    public String toString() {
        String arg1s = "", arg1uses = "", arg2s = "", arg2uses = "", results = "", resultuses = "";

        if (arg1 != null) {
            arg1s = arg1.toString();
            if (arg1 instanceof QuadrupleObjVar && Config.printIRCodeLivenessAnalysis)
                arg1uses = arg1NextUse.toString();
        }
        if (arg2 != null) {
            arg2s = arg2.toString();
            if (arg2 instanceof QuadrupleObjVar && Config.printIRCodeLivenessAnalysis)
                arg2uses = arg2NextUse.toString();
        }
        if (result != null) {
            results = result.toString();
            if (result instanceof QuadrupleObjVar && Config.printIRCodeLivenessAnalysis)
                resultuses = resultNextUse.toString();
        }

        String formattedOutput = String.format("%-10s | %-10s %-5s | %-10s %-5s | %-10s %-5s |",
                instruction, arg1s, arg1uses, arg2s, arg2uses, results, resultuses);

        return formattedOutput.toString();
    }
}
