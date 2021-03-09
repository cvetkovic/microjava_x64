package cvetkovic.optimizer.passes;

import cvetkovic.exceptions.UninitializedVariableException;
import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadruplePhi;
import cvetkovic.ir.quadruple.arguments.QuadrupleVariable;
import cvetkovic.ir.CodeSequence;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashSet;
import java.util.Set;

public class UninitializedVariableDetection implements OptimizerPass {

    private CodeSequence sequence;
    private Set<Obj> globalVariables;

    public UninitializedVariableDetection(CodeSequence sequence, Set<Obj> globalVariables) {
        this.sequence = sequence;
        this.globalVariables = globalVariables;
    }

    @Override
    public void optimize() {
        Set<Obj> uninitializedVariables = new HashSet<>();

        for (BasicBlock basicBlock : sequence.basicBlocks) {
            for (Quadruple instruction : basicBlock.instructions) {
                QuadrupleVariable arg1 = instruction.getArg1();
                QuadrupleVariable arg2 = instruction.getArg2();

                if (instruction.getInstruction() == IRInstruction.MALLOC)
                    continue;
                else if (instruction.getInstruction() == IRInstruction.INVOKE_VIRTUAL)
                    continue;

                if (arg1 instanceof QuadrupleObjVar && instruction.getSsaArg1Count() == 0) {
                    Obj obj = ((QuadrupleObjVar) arg1).getObj();

                    if (!obj.parameter && !obj.tempVar && !globalVariables.contains(obj))
                        uninitializedVariables.add(obj);
                } else if (arg2 instanceof QuadrupleObjVar && instruction.getSsaArg2Count() == 0) {
                    Obj obj = ((QuadrupleObjVar) arg2).getObj();

                    if (!obj.parameter && !obj.tempVar && !globalVariables.contains(obj))
                        uninitializedVariables.add(obj);
                } else if (arg1 instanceof QuadruplePhi) {
                    QuadruplePhi phi = (QuadruplePhi) arg1;
                    for (int i = 0; i < phi.size(); i++)
                        if (phi.getPhiArg(i) == 0 && !phi.getObj().parameter)
                            uninitializedVariables.add(phi.getObj());
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (Obj obj : uninitializedVariables)
            stringBuilder.append("Variable ").append(obj.getName()).append(" has not been defined on all code paths.").append(System.lineSeparator());

        if (uninitializedVariables.size() > 0)
            throw new UninitializedVariableException(stringBuilder.toString());
    }

    @Override
    public void finalizePass() {

    }
}
