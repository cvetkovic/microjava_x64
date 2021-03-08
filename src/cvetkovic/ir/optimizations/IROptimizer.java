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
    public IROptimizer(List<List<Quadruple>> code, List<Obj> functions, Set<Obj> globalVariables) {
        int i = 0;

        this.globalVariables = globalVariables;

        for (List<Quadruple> quadrupleList : code) {
            CodeSequence sequence = new CodeSequence();

            sequence.function = functions.get(i);
            sequence.labelIndices = BasicBlock.generateMapOfLabels(quadrupleList);
            sequence.basicBlocks = BasicBlock.extractBasicBlocksFromSequence(sequence.function, quadrupleList, sequence.labelIndices);

            assert sequence.basicBlocks != null;
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
                allVariables.addAll(b.extractAllVariables());
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

    public static int giveAddressToAll(Collection<Obj> variables, int startValue) {
        for (Obj obj : variables) {
            int lastTaken = startValue;
            if (SystemV_ABI.alignTo16(lastTaken) - lastTaken < SystemV_ABI.getX64VariableSize(obj.getType()))
                lastTaken = SystemV_ABI.alignTo16(lastTaken);
            int thisVarAddress = lastTaken + SystemV_ABI.getX64VariableSize(obj.getType());
            obj.setAdr(thisVarAddress);
            startValue = thisVarAddress;

        }

        return startValue;
    }

    public static int giveAddressToTemps(Collection<Obj> variables, int startValue) {
        for (Obj obj : variables) {
            if (((obj.tempVar || obj.getName().startsWith(Config.prefix_phi)) || (obj.parameter && !obj.stackParameter)) && obj.getKind() != Obj.Con) {
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

        for (CodeSequence codeSequence : codeSequenceList) {
            if (codeSequence.inlined)
                continue;

            stringBuilder.append("-----------------------------------------------------------------------------------------\n");

            for (BasicBlock basicBlock : IROptimizer.reassembleBasicBlocks(codeSequence.basicBlocks))
                for (Quadruple q : basicBlock.instructions)
                    stringBuilder.append(q).append(System.lineSeparator());

            stringBuilder.append("-----------------------------------------------------------------------------------------\n");
        }

        return stringBuilder.toString();
    }
}
