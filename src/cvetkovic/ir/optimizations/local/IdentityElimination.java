package cvetkovic.ir.optimizations.local;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.QuadrupleObjVar;
import cvetkovic.optimizer.OptimizerPass;

import java.util.ArrayList;
import java.util.List;

public class IdentityElimination implements OptimizerPass {

    protected BasicBlock basicBlock;

    public IdentityElimination(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    @Override
    public void optimize() {
        List<Quadruple> toRemove = new ArrayList<>();

        for (Quadruple q : basicBlock.instructions) {
            if (q.getInstruction() == IRInstruction.STORE) {
                // eliminate STORE var var, but not STORE var PTR var
                if (q.getArg2() != null)
                    continue; // STORE PTR instruction

                QuadrupleObjVar arg1 = (QuadrupleObjVar)q.getArg1();
                QuadrupleObjVar result = (QuadrupleObjVar)q.getResult();

                if (arg1.getObj() == result.getObj())
                    toRemove.add(q);
            }
        }

        for (Quadruple q : toRemove)
            basicBlock.instructions.remove(q);
    }

    @Override
    public void finalizePass() {

    }
}
