package cvetkovic.ir.optimizations;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.QuadrupleLabel;
import cvetkovic.ir.quadruple.QuadrupleVariable;

import java.util.*;

public class BasicBlock {
    public int basicBlockStart;
    public int basicBlockEnd;

    /**
     * Indexes labels in an instruction sequence MAP<LABEL_NAME, INDEX_IN_SEQUENCE>
     *
     * @param code IR instruction sequence
     */
    public static Map<String, Integer> generateMapOfLabels(List<Quadruple> code) {
        Map<String, Integer> result = new HashMap<>();

        for (int i = 0; i < code.size(); i++) {
            Quadruple q = code.get(i);

            if (q.getInstruction() == IRInstruction.GEN_LABEL)
                result.put(q.getArg1().toString(), i);
        }

        return result;
    }

    public static List<BasicBlock> extractBasicBlocksFromSequence(List<Quadruple> code, Map<String, Integer> labelIndices) {
        if (code.size() == 0)
            return null;

        List<BasicBlock> basicBlocks = new ArrayList<>();

        Set<Integer> leaders = new HashSet<>();
        leaders.add(0);

        for (int i = 0; i < code.size(); i++) {
            if (IRInstruction.isBasicBlockSplitInstruction(code.get(i).getInstruction())) {
                QuadrupleVariable destinationLabel = code.get(i).getResult();
                if (!(destinationLabel instanceof QuadrupleLabel))
                    throw new RuntimeException("Invalid format of branch instruction.");

                // adding destination of branch instruction to block leaders
                leaders.add(labelIndices.get(((QuadrupleLabel) destinationLabel).getLabelName()));

                // if it exists, add to leaders the first instruction after current branch
                if (i != code.size() - 1)
                    leaders.add(i);
            }
        }

        for (Integer l : leaders) {
            BasicBlock block = new BasicBlock();
            block.basicBlockStart = l;

            int end;
            for (end = l + 1; end < code.size() && !leaders.contains(end); end++);
            block.basicBlockEnd = end;

            basicBlocks.add(block);
        }

        return basicBlocks;
    }
}
