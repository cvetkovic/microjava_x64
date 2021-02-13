package cvetkovic.ir.optimizations;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleIntegerConst;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.misc.Config;
import cvetkovic.optimizer.CodeSequence;
import cvetkovic.optimizer.Optimizer;
import cvetkovic.x64.SystemV_ABI;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IROptimizer extends Optimizer {
    public IROptimizer(List<List<Quadruple>> code, List<Obj> functions) {
        int i = 0;

        for (List<Quadruple> quadrupleList : code) {
            CodeSequence sequence = new CodeSequence();

            sequence.function = functions.get(i);
            sequence.labelIndices = BasicBlock.generateMapOfLabels(quadrupleList);
            sequence.basicBlocks = BasicBlock.extractBasicBlocksFromSequence(sequence.function, quadrupleList, sequence.labelIndices);

            for (BasicBlock b : sequence.basicBlocks) {
                if (b.isEntryBlock()) {
                    sequence.entryBlock = b;
                    break;
                }
            }
            if (sequence.entryBlock == null)
                throw new RuntimeException("Invalid code sequence for loop discovery as entry block has not been found.");

            // update ENTER instruction and assign address to all variables
            Quadruple enterInstruction = sequence.entryBlock.instructions.get(1);

            Collection<Obj> allVariables = new HashSet<>();
            for (BasicBlock b : sequence.basicBlocks) {
                allVariables.addAll(extractAllVariables(b.instructions));
                allVariables.addAll(new HashSet<>(sequence.function.getLocalSymbols()));
            }

            int oldAllocationValue = ((QuadrupleIntegerConst) enterInstruction.getArg1()).getValue();

            //System.out.println("Variables for " + sequence.function.getName());
            int lastSize = giveAddressToTemps(allVariables, oldAllocationValue);

            enterInstruction.setArg1(new QuadrupleIntegerConst(SystemV_ABI.alignTo16(lastSize)));

            codeSequenceList.add(sequence);
            i++;
        }
    }

    private Collection<Obj> extractAllVariables(List<Quadruple> allInstructions) {
        Set<Obj> objs = new HashSet<>();

        for (Quadruple q : allInstructions) {
            Obj obj1 = (q.getArg1() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) q.getArg1()).getObj() : null);
            Obj obj2 = (q.getArg2() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) q.getArg2()).getObj() : null);
            Obj objResult = (q.getResult() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) q.getResult()).getObj() : null);

            if (obj1 != null && obj1.getKind() != Obj.Con)
                objs.add(obj1);

            if (obj2 != null && obj2.getKind() != Obj.Con)
                objs.add(obj2);

            if (objResult != null && objResult.getKind() != Obj.Con)
                objs.add(objResult);
        }

        return objs;
    }

    public static int giveAddressToTemps(Collection<Obj> variables, int startValue) {
        for (Obj obj : variables) {
            if (((obj.tempVar || obj.getName().startsWith(Config.prefix_phi)) || (obj.parameter && obj.stackParameter == false)) && obj.getKind() != Obj.Con) {
                int lastTaken = startValue;
                if (SystemV_ABI.alignTo16(lastTaken) - lastTaken < SystemV_ABI.getX64VariableSize(obj.getType()))
                    lastTaken = SystemV_ABI.alignTo16(lastTaken);
                int thisVarAddress = lastTaken + SystemV_ABI.getX64VariableSize(obj.getType());
                obj.setAdr(thisVarAddress);
                startValue = thisVarAddress;
            }
        }

        return startValue;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < codeSequenceList.size(); i++) {
            stringBuilder.append("-----------------------------------------------------------------------------------------\n");

            CodeSequence sequence = codeSequenceList.get(i);
            for (BasicBlock basicBlock : IROptimizer.reassembleBasicBlocks(sequence.basicBlocks))
                for (Quadruple q : basicBlock.instructions)
                    stringBuilder.append(q + "\n");

            stringBuilder.append("-----------------------------------------------------------------------------------------\n");
        }

        return stringBuilder.toString();
    }
}
