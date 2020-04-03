package cvetkovic.ir.optimizations;

import cvetkovic.ir.quadruple.Quadruple;

public class BasicBlockInstruction {
    public enum InstructionState
    {
        ALIVE,
        DEAD
    }

    private Quadruple quadruple;
    private BasicBlockInstruction nextInstruction;
}
