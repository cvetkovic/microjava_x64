package cvetkovic.x64.cpu;

import rs.etf.pp1.symboltable.concepts.Obj;

public abstract class Descriptor {
    protected Obj holdsValueOf;

    public Obj getHoldsValueOf() {
        return holdsValueOf;
    }

    public void setHoldsValueOf(Obj holdsValueOf) {
        this.holdsValueOf = holdsValueOf;
    }

    @Override
    public abstract String toString();
}
