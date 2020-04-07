package cvetkovic.ir.optimizations.local;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.List;

public class CommonSubexpressionElimination implements OptimizerPass {

    protected BasicBlock basicBlock;
    protected List<Obj> allVariables;



    @Override
    public void doOptimization() {
        
    }
}
