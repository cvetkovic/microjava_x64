package cvetkovic.ir.optimizations;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.QuadrupleObjVar;
import cvetkovic.ir.quadruple.QuadrupleVariable;
import cvetkovic.optimizer.Optimizer;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;
import java.util.stream.Collectors;

public class BasicBlock {

    //////////////////////////////////////////////////////////////////////////////////
    // CLASS FIELDS
    //////////////////////////////////////////////////////////////////////////////////

    private static int blockCounter = 0;
    public int blockId = blockCounter++;

    public int basicBlockStart;
    public int basicBlockEnd;

    public List<BasicBlock> predecessor = new ArrayList<>();
    public List<BasicBlock> successor = new ArrayList<>(2);

    //////////////////////////////////////////////////////////////////////////////////
    // STATIC FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////

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

    private static Collection<Obj> extractAllVariables(Optimizer.CodeSequence sequence, BasicBlock basicBlock) {
        Set<Obj> variables = new HashSet<>();

        for (int i = basicBlock.basicBlockStart; i <= basicBlock.basicBlockEnd; i++) {
            QuadrupleVariable arg1 = sequence.code.get(i).getArg1();
            QuadrupleVariable arg2 = sequence.code.get(i).getArg2();
            QuadrupleVariable result = sequence.code.get(i).getResult();

            if (arg1 instanceof QuadrupleObjVar)
                variables.add(((QuadrupleObjVar) arg1).getObj());

            if (arg2 instanceof QuadrupleObjVar)
                variables.add(((QuadrupleObjVar) arg2).getObj());

            if (result instanceof QuadrupleObjVar)
                variables.add(((QuadrupleObjVar) result).getObj());
        }

        return variables;
    }

    public static void determineNextUse(Optimizer.CodeSequence sequence) {
        for (BasicBlock basicBlock : sequence.basicBlocks) {
            // extract object nodes of all operands and destination variables in the basic block
            Collection<Obj> allVariablesUsedInThisBlock = extractAllVariables(sequence, basicBlock);
            // split non-temporary and temporary variables into separate sets
            Collection<Obj> nonTemporaryVariables = allVariablesUsedInThisBlock.stream().filter(p -> !p.tempVar).collect(Collectors.toSet());
            Collection<Obj> temporaryVariables = allVariablesUsedInThisBlock.stream().filter(p -> p.tempVar).collect(Collectors.toSet());

            // TODO: change whole IRCodeGenerator to set tempVar to true where needed

            // insert all the variables into this map and set all those non-temporary to alive, other the dead
            Map<Obj, Quadruple.NextUseState> liveness = new HashMap<>();
            nonTemporaryVariables.forEach(p -> liveness.put(p, Quadruple.NextUseState.ALIVE));
            temporaryVariables.forEach(p -> liveness.put(p, Quadruple.NextUseState.DEAD));

            for (int instructionIndex = basicBlock.basicBlockEnd; instructionIndex > basicBlock.basicBlockStart; instructionIndex--) {
                Quadruple instruction = sequence.code.get(instructionIndex);

                Obj obj1 = null, obj2 = null, objResult = null;

                if (instruction.getArg1() instanceof QuadrupleObjVar) {
                    obj1 = ((QuadrupleObjVar) instruction.getArg1()).getObj();
                    instruction.setArg1NextUse(liveness.get(obj1));
                }
                if (instruction.getArg2() instanceof QuadrupleObjVar) {
                    obj2 = ((QuadrupleObjVar) instruction.getArg2()).getObj();
                    instruction.setArg2NextUse(liveness.get(obj2));
                }
                if (instruction.getResult() instanceof QuadrupleObjVar) {
                    objResult = ((QuadrupleObjVar) instruction.getResult()).getObj();
                    instruction.setResultNextUse(liveness.get(objResult));
                }

                if (obj1 != null)
                    liveness.put(obj1, Quadruple.NextUseState.ALIVE);
                if (obj2 != null)
                    liveness.put(obj2, Quadruple.NextUseState.ALIVE);
                if (objResult != null)
                    liveness.put(objResult, Quadruple.NextUseState.DEAD);
            }
        }
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

                    // TODO: here a bug lies -> only last successor will be traversed -> introduce stack/queue

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

        // TODO: find more efficient algorithm for finding (non)elementary cycles in a graph

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

    //////////////////////////////////////////////////////////////////////////////////
    // FUNCTION MEMBERS
    //////////////////////////////////////////////////////////////////////////////////

    public String printBasicBlock(List<Quadruple> code) {
        StringBuilder sb = new StringBuilder();

        for (int i = basicBlockStart; i <= basicBlockEnd; i++)
            sb.append(code.get(i)).append(System.lineSeparator());

        return sb.toString();
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