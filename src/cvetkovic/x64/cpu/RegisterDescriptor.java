package cvetkovic.x64.cpu;

import rs.etf.pp1.symboltable.concepts.Obj;

public class RegisterDescriptor extends Descriptor {
    protected String ISAName;

    public RegisterDescriptor(String ISAName) {
        this.ISAName = ISAName;
    }

    public Obj getHoldsValueOf() {
        return holdsValueOf;
    }

    public void setHoldsValueOf(Obj holdsValueOf) {
        this.holdsValueOf = holdsValueOf;
    }
}
