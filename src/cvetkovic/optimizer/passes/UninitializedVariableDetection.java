package cvetkovic.optimizer.passes;

import cvetkovic.exceptions.UninitializedVariableException;
import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadruplePhi;
import cvetkovic.ir.quadruple.arguments.QuadrupleVariable;
import cvetkovic.ir.CodeSequence;
import cvetkovic.misc.Config;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashSet;
import java.util.Set;

public class UninitializedVariableDetection implements OptimizerPass {

    private final CodeSequence sequence;
    private final Set<Obj> globalVariables;

    private final Set<QuadruplePhi> markedDeadPhis = new HashSet<>();

    public UninitializedVariableDetection(CodeSequence sequence, Set<Obj> globalVariables) {
        this.sequence = sequence;
        this.globalVariables = globalVariables;

        determineDeadPhis();
    }

    private void determineDeadPhis() {
        for (BasicBlock basicBlock : sequence.basicBlocks) {
            for (Quadruple instruction : basicBlock.instructions) {
                if (instruction.getInstruction() == IRInstruction.STORE_PHI) {
                    Obj resultObj = ((QuadrupleObjVar) instruction.getResult()).getObj();

                    boolean dead = true;
                    for (BasicBlock basicBlock2 : sequence.basicBlocks) {
                        for (Quadruple i2 : basicBlock2.instructions) {
                            if (instruction == i2)
                                continue;

                            if (i2.getArg1() instanceof QuadrupleObjVar) {
                                Obj obj = ((QuadrupleObjVar) i2.getArg1()).getObj();

                                if (resultObj == obj && instruction.getSsaResultCount() == i2.getSsaArg1Count()) {
                                    dead = false;
                                    break;
                                }
                            } else if (i2.getArg1() instanceof QuadruplePhi) {
                                Obj obj = ((QuadruplePhi) i2.getArg1()).getObj();

                                if (resultObj == obj && ((QuadruplePhi) i2.getArg1()).contains(instruction.getSsaResultCount()))
                                    dead = false;
                                break;
                            }

                            if (i2.getArg2() instanceof QuadrupleObjVar) {
                                Obj obj = ((QuadrupleObjVar) i2.getArg2()).getObj();

                                if (resultObj == obj && instruction.getSsaResultCount() == i2.getSsaArg2Count()) {
                                    dead = false;
                                    break;
                                }
                            }
                        }
                    }

                    if (dead)
                        markedDeadPhis.add((QuadruplePhi) instruction.getArg1());
                }
            }
        }
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

                    if (markedDeadPhis.contains(phi))
                        continue;

                    for (int i = 0; i < phi.size(); i++) {
                        if (phi.getObj().inlinedParameter)
                            assert !phi.getObj().parameter;

                        if (phi.getPhiArg(i) == 0 && !phi.getObj().parameter && !phi.getObj().inlinedParameter)
                            uninitializedVariables.add(phi.getObj());
                    }
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();

        if (uninitializedVariables.size() > 0)
            stringBuilder.append("Program contains uninitialized variables, " +
                    "therefore it might exhibit non-deterministic semantics.").append(System.lineSeparator());

        Set<String> reducedSet = new HashSet<>();
        for (Obj obj : uninitializedVariables) {
            String objName = obj.getName();
            String varname = objName;

            if (objName.contains(Config.inlinedVarClonesPrefix))
                varname = objName.substring(0, objName.indexOf(Config.inlinedVarClonesPrefix));

            reducedSet.add(varname);
        }

        reducedSet.forEach(p -> stringBuilder.append("Variable '").
                append(p).append("' in function '").
                append(sequence.function.getName()).
                append("' has not been defined on all code paths.").append(System.lineSeparator()));

        if (uninitializedVariables.size() > 0)
            throw new UninitializedVariableException(stringBuilder.toString());
    }

    @Override
    public void finalizePass() {

    }
}
