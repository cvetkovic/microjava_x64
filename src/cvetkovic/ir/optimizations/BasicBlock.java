package cvetkovic.ir.optimizations;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.quadruple.Quadruple;

import java.util.*;

public class BasicBlock {
    private static int blockCounter = 0;
    public int blockId = blockCounter++;

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

    /**
     * Applies loop detection criteria as in [ALSU06], pg. 531
     */
    public static List<Set<BasicBlock>> discoverLoops(List<Set<BasicBlock>> loops) {
        List<Set<BasicBlock>> result = new ArrayList<>();

        for (Set<BasicBlock> set : loops) {
            BasicBlock entryNode = null;

            if (set.size() <= 1)    // third criteria
                continue;

            int countWithoutPredecessorsOutsideL = 0;
            for (BasicBlock b : set) {
                int cnt = 0;

                for (int i = 0; i < b.predecessor.size(); i++)
                    if (set.contains(b.predecessor.get(i)))
                        cnt++;
                    else
                        entryNode = b;

                if (cnt == b.predecessor.size())
                    countWithoutPredecessorsOutsideL++;
            }

            if (set.size() - 1 != countWithoutPredecessorsOutsideL) // second criteria
                continue;

            if (!entryNode.isEntryBlock())  // first criteria
                result.add(set);
        }

        return result;
    }

    /**
     * Eliminates edges vertices that are not in cycle
     */
    private static Set<BasicBlock> traverseLoopToEliminateUnnecessaryVertices(BasicBlock beginFrom, Set<BasicBlock> blocks) {
        Set<BasicBlock> minimumSet = new HashSet<>();
        BasicBlock current = beginFrom;

        while (true) {
            for (int i = 0; i < current.successor.size(); i++) {
                if (blocks.contains(current.successor.get(i))) {
                    minimumSet.add(current);
                    current = current.successor.get(i);

                    break;
                }
            }

            if (current == beginFrom)
                break;
        }

        return minimumSet;
    }

    /**
     * Detection of cycles in control flow graph
     */
    public static List<Set<BasicBlock>> discoverCycles(BasicBlock enterBlock) {
        List<Set<BasicBlock>> result = new ArrayList<>();

        Stack<BasicBlock> blocksToVisit = new Stack<>();
        Stack<Set<BasicBlock>> visited = new Stack<>();

        Set<BasicBlock> initial = new HashSet<>();
        visited.push(initial);

        blocksToVisit.push(enterBlock);
        while (!blocksToVisit.empty()) {
            BasicBlock current = blocksToVisit.pop();
            Set<BasicBlock> currentVisited = visited.pop();

            if (currentVisited.contains(current)) {
                result.add(traverseLoopToEliminateUnnecessaryVertices(current, currentVisited));
                continue;
            }
            else
                currentVisited.add(current);

            for (BasicBlock b : current.successor) {
                blocksToVisit.push(b);
                Set<BasicBlock> copyOfVisited = new HashSet<>();
                copyOfVisited.addAll(currentVisited);
                visited.push(copyOfVisited);
            }
        }

        // so far all elementary cycles had been found
        // we need to find non-elementary cycles as well

        int lastAdd = 0;
        int currentAdd = -1;
        List<Set<BasicBlock>> toAdd = new ArrayList<>();

        while (lastAdd != currentAdd) {
            currentAdd = 0;
            for (Set<BasicBlock> set1 : result) {
                for (int i = 0; i < result.size(); i++) {
                    Set<BasicBlock> set2 = result.get(i);

                    if (set1 != set2) {
                        for (BasicBlock b : set2) {
                            if (set1.contains(b)) {
                                // try merging if result doesn't contain this
                                Set<BasicBlock> newSet = new HashSet<>();
                                newSet.addAll(set1);
                                newSet.addAll(set2);

                                boolean add = true;
                                for (int x = 0; x < result.size(); x++) {
                                    if (result.get(x).equals(newSet) || toAdd.contains(newSet))
                                        add = false;
                                }

                                if (add) {
                                    toAdd.add(newSet);
                                    currentAdd++;
                                }
                            }
                        }
                    }
                }
            }

            result.addAll(toAdd);
            toAdd = new ArrayList<>();
            if (currentAdd != lastAdd) {
                lastAdd = currentAdd;
                currentAdd = -1;
            }
            else
                break;
        }

        return result;
    }

    public static List<BasicBlock> extractBasicBlocksFromSequence
            (List<Quadruple> code, Map<String, Integer> labelIndices) {
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

    public boolean isEntryBlock() {
        return predecessor.size() == 0;
    }

    public boolean isExitBlock() {
        return successor.size() == 0;
    }

    @Override
    public String toString() {
        final StringBuilder p = new StringBuilder();
        p.append("(");
        predecessor.forEach(x -> p.append(x.blockId).append(", "));
        p.append(")");

        final StringBuilder s = new StringBuilder();
        s.append("(");
        successor.forEach(x -> s.append(x.blockId).append(", "));
        s.append(")");

        return "[id = " + blockId +
                ", instructions = (" + basicBlockStart + ", " + basicBlockEnd + ")" +
                ", start = " + isEntryBlock() +
                ", end = " + isExitBlock() +
                ", predecessors = " + p.toString().replace(", )", ")") +
                ", successors = " + s.toString().replace(", )", ")") + "]";
    }
}
