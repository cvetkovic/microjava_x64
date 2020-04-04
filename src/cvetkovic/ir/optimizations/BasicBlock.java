package cvetkovic.ir.optimizations;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.quadruple.Quadruple;

import java.util.*;

public class BasicBlock {
    private static int blockCounter = 0;
    private int blockId = blockCounter++;

    public int basicBlockStart;
    public int basicBlockEnd;

    public List<BasicBlock> predecessor = new ArrayList<>();
    public List<BasicBlock> successor = new ArrayList<>(2);

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

    private static class Tuple<U, V> {
        U u;
        V v;

        public Tuple(U u, V v) {
            this.u = u;
            this.v = v;
        }
    }

    public static List<BasicBlock> extractBasicBlocksFromSequence(List<Quadruple> code, Map<String, Integer> labelIndices) {
        if (code.size() == 0)
            return null;

        blockCounter = 0;

        List<BasicBlock> basicBlocks = new ArrayList<>();

        Set<Integer> leaders = new HashSet<>();
        leaders.add(0);

        // find leaders -> they will be the first instruction in a basic block
        for (int i = 0; i < code.size(); i++) {
            Quadruple quadruple = code.get(i);

            if (IRInstruction.isBasicBlockSplitInstruction(quadruple.getInstruction())) {
                String destinationLabel = quadruple.getResult().toString();

                // adding destination of branch instruction to block leaders
                leaders.add(labelIndices.get(destinationLabel));

                // if it exists, add to leaders the first instruction after current branch
                if (i != code.size() - 1)
                    leaders.add(i + 1);
            }
        }

        List<Tuple<BasicBlock, List<Integer>>> followerList = new ArrayList<>();
        Map<Integer, BasicBlock> firstInstruction = new HashMap<>();

        // extract basic blocks
        for (Integer l : leaders) {
            // set first instruction
            BasicBlock block = new BasicBlock();
            block.basicBlockStart = l;

            // set last instruction
            int end;
            for (end = l + 1; end < code.size() && !leaders.contains(end); end++) ;
            block.basicBlockEnd = end - 1;

            // find predecessors and successors
            basicBlocks.add(block);
            firstInstruction.put(block.basicBlockStart, block);

            List<Integer> followers = new LinkedList<>();

            Quadruple lastInstruction = code.get(block.basicBlockEnd);
            if (lastInstruction.getInstruction() == IRInstruction.JMP)
                // one successor only
                followers.add(labelIndices.get(lastInstruction.getResult().toString()));
            else if (lastInstruction.getInstruction() == IRInstruction.LEAVE)
                // no successor
                continue;
            else if (IRInstruction.isBasicBlockSplitInstruction(lastInstruction.getInstruction()) &&
                    lastInstruction.getInstruction() != IRInstruction.JMP) {
                // two successors
                followers.add(labelIndices.get(lastInstruction.getResult().toString()));
                followers.add(block.basicBlockEnd + 1);
            }
            else
                // no jump instruction -> ELSE branch of IF statement -> falling through like in C switch statement
                followers.add(block.basicBlockEnd + 1);

            followerList.add(new Tuple(block, followers));

            // DO NOT PUT ANYTHING HERE OR MOVE CONTINUE STATEMENT BELOW
        }

        // link predecessors and successors list
        for (Tuple<BasicBlock, List<Integer>> t : followerList) {
            BasicBlock b = t.u;
            List<Integer> followers = t.v;

            for (Integer i : followers) {
                b.successor.add(firstInstruction.get(i));
                firstInstruction.get(i).predecessor.add(b);
            }
        }

        return basicBlocks;
    }

    private boolean isEntryBlock() {
        return predecessor.size() == 0;
    }

    private boolean isExitBlock() {
        return successor.size() == 0;
    }

    @Override
    public String toString() {
        final StringBuilder p = new StringBuilder();
        p.append("(");
        predecessor.forEach(x -> p.append(x.blockId + ", "));
        p.append(")");

        final StringBuilder s = new StringBuilder();
        s.append("(");
        successor.forEach(x -> s.append(x.blockId + ", "));
        s.append(")");

        return "[id = " + blockId +
                ", instructions = (" + basicBlockStart + ", " + basicBlockEnd + ")" +
                ", start = " + isEntryBlock() +
                ", end = " + isExitBlock() +
                ", predecessors = " + p.toString().replace(", )", ")") +
                ", successors = " + s.toString().replace(", )", ")") + "]";
    }
}
