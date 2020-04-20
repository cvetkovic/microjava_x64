package cvetkovic.ir.optimizations;

import cvetkovic.ir.optimizations.local.LocalValueNumbering;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleIntegerConst;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.Optimizer;
import cvetkovic.x64.SystemV_ABI;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IROptimizer extends Optimizer {
    public IROptimizer(List<List<Quadruple>> code, List<Obj> functions) {
        int i = 0;

        for (List<Quadruple> quadrupleList : code) {
            CodeSequence sequence = new CodeSequence();

            sequence.function = functions.get(i);
            sequence.code = quadrupleList;
            sequence.labelIndices = BasicBlock.generateMapOfLabels(quadrupleList);
            sequence.basicBlocks = BasicBlock.extractBasicBlocksFromSequence(sequence.function, quadrupleList, sequence.labelIndices);

            for (BasicBlock b : sequence.basicBlocks)
                if (b.isEntryBlock())
                    sequence.entryBlock = b;
            if (sequence.entryBlock == null)
                throw new RuntimeException("Invalid code sequence for loop discovery as entry block has not been found.");

            // update ENTER instruction and assign address to all variables
            Quadruple enterInstruction = sequence.entryBlock.instructions.get(1);

            Collection<Obj> allVariables = extractAllVariables(sequence.code);
            allVariables.addAll(sequence.function.getLocalSymbols().stream().collect(Collectors.toSet()));
            int sizeOfTempVars = calculateSizeOfTempVariables(allVariables);
            int oldAllocationValue = ((QuadrupleIntegerConst)enterInstruction.getArg1()).getValue();

            int sizeToAllocate = oldAllocationValue + sizeOfTempVars;
            // because rsp + 0 -> is old ebp, offset 8 is the first element on stack
            int lastSize = giveAddressToTemps(allVariables, sizeToAllocate);

            enterInstruction.setArg1(new QuadrupleIntegerConst(SystemV_ABI.alignTo16(lastSize)));

            codeSequenceList.add(sequence);
            i++;
        }

        createOptimizationList();
    }

    private int calculateSizeOfTempVariables(Collection<Obj> variables) {
        int tempVarSize = 0;

        List<Obj> tempVars = variables.stream().filter(p -> p.tempVar).collect(Collectors.toList());
        for (Obj obj : tempVars)
            tempVarSize += SystemV_ABI.getX64VariableSize(obj.getType());

        return tempVarSize;
    }

    private Collection<Obj> extractAllVariables(List<Quadruple> allInstructions) {
        Set<Obj> objs = new HashSet<>();

        for (Quadruple q : allInstructions) {
            Obj obj1 = (q.getArg1() instanceof QuadrupleObjVar ? ((QuadrupleObjVar)q.getArg1()).getObj() : null);
            Obj obj2 = (q.getArg2() instanceof QuadrupleObjVar ? ((QuadrupleObjVar)q.getArg2()).getObj() : null);
            Obj objResult = (q.getResult() instanceof QuadrupleObjVar ? ((QuadrupleObjVar)q.getResult()).getObj() : null);

            if (obj1 != null && obj1.getKind() != Obj.Con)
                objs.add(obj1);

            if (obj2 != null && obj2.getKind() != Obj.Con)
                objs.add(obj2);

            if (objResult != null && objResult.getKind() != Obj.Con)
                objs.add(objResult);
        }

        return objs;
    }

    private int giveAddressToTemps(Collection<Obj> variables, int startValue) {
        for (Obj obj : variables) {
            if ((obj.tempVar || (obj.parameter && obj.stackParameter == false)) && obj.getKind() != Obj.Con) {
                int objSize = SystemV_ABI.getX64VariableSize(obj.getType());
                obj.setAdr(startValue + objSize);
                startValue += objSize;
            }

            System.out.println(obj.getName() + " -> " + obj.getAdr());
        }

        System.out.println();

        return startValue;
    }

    private void createOptimizationList() {
        for (CodeSequence sequence : codeSequenceList) {
            // LOCAL OPTIMIZATIONS
            for (BasicBlock block : sequence.basicBlocks) {
                addOptimizationPass(new LocalValueNumbering(block));
                //addOptimizationPass(new IdentityElimination(block));
                //addOptimizationPass(new DeadCodeElimination(block));
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < codeSequenceList.size(); i++) {
            stringBuilder.append("---------------------------------------------------------------------\n");

            CodeSequence sequence = codeSequenceList.get(i);
            for (Quadruple q : sequence.code)
                stringBuilder.append(q + "\n");

            stringBuilder.append("---------------------------------------------------------------------\n");
        }

        return stringBuilder.toString();
    }
}
