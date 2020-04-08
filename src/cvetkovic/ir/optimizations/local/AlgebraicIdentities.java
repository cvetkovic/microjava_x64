package cvetkovic.ir.optimizations.local;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.optimizer.OptimizerPass;

public class AlgebraicIdentities implements OptimizerPass {
    protected BasicBlock basicBlock;

    public AlgebraicIdentities(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    @Override
    public void optimize() {
        for (Quadruple q : basicBlock.instructions) {
            switch (q.getInstruction()) {
                case ADD:
                    // TODO: resolve x + 0 = 0 + x

                    break;
                case SUB:
                    // TODO: resolve x - 0

                    break;
                case MUL:
                    // var * 2 = var + var -> not needed because it will be impelemented in machine code as SHL var 1
                    // TODO: resolve x * 1 = 1 * x

                    break;
                case DIV:
                    // TODO: resolve x / 1
                    break;
            }
        }
    }

    @Override
    public void finalizePass() {

    }
}
