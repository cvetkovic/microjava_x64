package cvetkovic.ir.quadruple;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadrupleVariable;
import cvetkovic.misc.Config;
import rs.etf.pp1.symboltable.concepts.Obj;

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

    protected int ssaArg1Count = -1;
    protected int ssaArg2Count = -1;
    protected int ssaResultCount = -1;

    protected int phiID = -1;

    public Quadruple(IRInstruction instruction) {
        this.instruction = instruction;
    }

    public Quadruple(IRInstruction instruction, QuadrupleVariable arg1, QuadrupleVariable arg2) {
        this.instruction = instruction;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public Quadruple makeClone() {
        Quadruple q = new Quadruple(instruction);

        if (arg1 != null)
            q.setArg1(arg1.makeClone());
        if (arg2 != null)
            q.setArg2(arg2.makeClone());
        if (result != null)
            q.setResult(result.makeClone());

        return q;
    }

    public void setSSACountArg1(int i) {
        ssaArg1Count = i;
    }

    public void setSSACountArg2(int i) {
        ssaArg2Count = i;
    }

    public void setSSACountResult(int i) {
        ssaResultCount = i;
    }

    public int getPhiID() {
        return phiID;
    }

    public void setPhiID(int phiID) {
        this.phiID = phiID;
    }

    public int getSsaArg1Count() {
        return ssaArg1Count;
    }

    public int getSsaArg2Count() {
        return ssaArg2Count;
    }

    public int getSsaResultCount() {
        return ssaResultCount;
    }

    public void SSAToNormalForm() {
        ssaArg1Count = -1;
        ssaArg2Count = -1;
        ssaResultCount = -1;
    }

    public void setInstruction(IRInstruction add) {
        this.instruction = add;
    }

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
        String arg1s = "", arg1Addr = "", arg2s = "", arg2Addr = "", results = "", resultAddr = "";
        String flags1 = "___", flags2 = "___", flagsR = "___";

        if (arg1 != null) {
            arg1s = arg1.toString();

            if (ssaArg1Count != -1)
                arg1s += "_" + ssaArg1Count;

            if (arg1 instanceof QuadrupleObjVar) {
                Obj obj = ((QuadrupleObjVar) arg1).getObj();

                if (obj.getKind() == Obj.Var)
                    arg1Addr = "(" + obj.getAdr() + ")";
                flags1 = (obj.parameter ? "P" : "_");
                flags1 += (obj.inlined ? "I" : "_");
                flags1 += (obj.tempVar ? "T" : "_");
            }
        }
        if (arg2 != null) {
            arg2s = arg2.toString();

            if (ssaArg2Count != -1)
                arg2s += "_" + ssaArg2Count;

            if (arg2 instanceof QuadrupleObjVar) {
                Obj obj = ((QuadrupleObjVar) arg2).getObj();

                if (obj.getKind() == Obj.Var)
                    arg2Addr = "(" + obj.getAdr() + ")";
                flags2 = (obj.parameter ? "P" : "_");
                flags2 += (obj.inlined ? "I" : "_");
                flags2 += (obj.tempVar ? "T" : "_");
            }
        }
        if (result != null) {
            results = result.toString();

            if (ssaResultCount != -1)
                results += "_" + ssaResultCount;

            if (result instanceof QuadrupleObjVar) {
                Obj obj = ((QuadrupleObjVar) result).getObj();

                if (obj.getKind() == Obj.Var)
                    resultAddr = "(" + obj.getAdr() + ")";
                flagsR = (obj.parameter ? "P" : "_");
                flagsR += (obj.inlined ? "I" : "_");
                flagsR += (obj.tempVar ? "T" : "_");
            }
        }

        return String.format("%-15.15s | %-15.15s %-5s (%-3s) | %-15.15s %-5s (%-3s) | %-15.15s %-5s (%-3s) |",
                instruction, arg1s, arg1Addr, flags1, arg2s, arg2Addr, flags2, results, resultAddr, flagsR);
    }

    public String getNonformattedOutput() {
        StringBuilder sb = new StringBuilder();

        sb.append(instruction).append(" ");
        if (arg1 != null) {
            sb.append(arg1.toString());
            if (ssaArg1Count != -1)
                sb.append("_").append(ssaArg1Count);
            if (arg2 != null && result != null)
                sb.append(", ");
        }

        if (arg2 != null) {
            if (result == null)
                sb.append(", ");
            sb.append(arg2.toString());
            if (ssaArg2Count != -1)
                sb.append("_").append(ssaArg2Count);
            if (result != null)
                sb.append(", ");
        } else if (result != null)
            sb.append(", ");

        if (result != null) {
            sb.append(result.toString());
            if (ssaResultCount != -1)
                sb.append("_").append(ssaResultCount);
        }

        return sb.toString();
    }
}
