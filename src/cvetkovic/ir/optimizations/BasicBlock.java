package cvetkovic.ir.optimizations;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadrupleVariable;
import cvetkovic.misc.Config;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;
import java.util.stream.Collectors;

public class BasicBlock {

    //////////////////////////////////////////////////////////////////////////////////
    // CLASS FIELDS
    //////////////////////////////////////////////////////////////////////////////////

    private static int blockCounter = 0;
    public int blockId = blockCounter++;

    public List<BasicBlock> predecessor = new ArrayList<>();
    public List<BasicBlock> successor = new ArrayList<>(2);

    public List<Quadruple> instructions = new ArrayList<>();

    public Collection<Obj> temporaryVariables;
    public Collection<Obj> allVariables;
    public Collection<Obj> nonTemporaryVariables;

    public Obj enclosingFunction;

    public BasicBlock(Obj enclosingFunction) {
        this.enclosingFunction = enclosingFunction;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // BASIC BLOCK INFORMATION EXTRACTION
    //////////////////////////////////////////////////////////////////////////////////

    private void prepareBasicBlockClass(List<Quadruple> quadrupleList) {
        if (Config.printBasicBlockInfo) {
            System.out.println("Basic blocks in function '" + quadrupleList.get(0).getArg1() + "':");
            System.out.println(this);
            System.out.println(printBasicBlock());
        }

        // extract object nodes of all operands and destination variables in the basic block
        allVariables = extractAllVariables();
        // split non-temporary and temporary variables into separate sets
        nonTemporaryVariables = allVariables.stream().filter(p -> !p.tempVar).collect(Collectors.toSet());
        temporaryVariables = allVariables.stream().filter(p -> p.tempVar).collect(Collectors.toSet());
    }

    /**
     * Inserts the instruction after the label
     */
    public void insertInstruction(Quadruple toInsert) {
        int indexAt = 0;

        for (Quadruple q : instructions)
            if (q.getInstruction() == IRInstruction.GEN_LABEL)
                indexAt++;

        instructions.add(indexAt, toInsert);
    }

    //////////////////////////////////////////////////////////////////////////////////
    // STATIC METHODS
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

    public static class Tuple<U, V> {
        public U u;
        public V v;

        public Tuple(U u, V v) {
            this.u = u;
            this.v = v;
        }
    }

    public static List<BasicBlock> extractBasicBlocksFromSequence(Obj function, List<Quadruple> code, Map<String, Integer> labelIndices) {
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
                Integer destination = labelIndices.get(destinationLabel);
                if (destination != null)
                    leaders.add(labelIndices.get(destinationLabel));
                else
                    throw new RuntimeException("Jump destination cannot be null. Error splitting code into basic blocks.");

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
            BasicBlock block = new BasicBlock(function);
            //block.firstQuadruple = l;
            block.instructions.add(code.get(l));

            // set last instruction
            int end;
            for (end = l + 1; end < code.size() && !leaders.contains(end); end++)
                block.instructions.add(code.get(end));
            int lastQuadruple = end - 1;

            block.prepareBasicBlockClass(code);

            // find predecessors and successors
            basicBlocks.add(block);
            firstInstruction.put(l, block);

            List<Integer> followers = new LinkedList<>();

            Quadruple lastInstruction = code.get(lastQuadruple);
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
                followers.add(lastQuadruple + 1);
            } else
                // no jump instruction -> ELSE branch of IF statement -> falling through like in C switch statement
                followers.add(lastQuadruple + 1);

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

    private Collection<Obj> extractAllVariables() {
        Set<Obj> variables = new HashSet<>();

        for (Quadruple q : instructions) {
            QuadrupleVariable arg1 = q.getArg1();
            QuadrupleVariable arg2 = q.getArg2();
            QuadrupleVariable result = q.getResult();

            if (arg1 instanceof QuadrupleObjVar)
                variables.add(((QuadrupleObjVar) arg1).getObj());

            if (arg2 instanceof QuadrupleObjVar)
                variables.add(((QuadrupleObjVar) arg2).getObj());

            if (result instanceof QuadrupleObjVar)
                variables.add(((QuadrupleObjVar) result).getObj());
        }

        return variables;
    }

    public String printBasicBlock() {
        StringBuilder sb = new StringBuilder();

        for (Quadruple q : instructions)
            sb.append(q).append(System.lineSeparator());

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
                ", start = " + isEntryBlock() +
                ", end = " + isExitBlock() +
                ", predecessors = " + p.toString().replace(", )", ")") +
                ", successors = " + s.toString().replace(", )", ")") + "]";
    }

    public Set<Obj> getSetOfDefinedVariables() {
        Set<Obj> result = new HashSet<>();

        for (Quadruple q : instructions) {
            switch (q.getInstruction()) {
                // arithmetic
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case REM:
                case NEG:
                    // memory
                case LOAD:
                case STORE:
                case MALLOC:
                case ALOAD:
                case ASTORE:
                case GET_PTR:
                    // functions
                case CALL:
                case INVOKE_VIRTUAL:
                    // I/O
                case SCANF:
                    result.add(((QuadrupleObjVar) q.getResult()).getObj());
                    break;
                default:
                    break;
            }
        }

        return result;
    }
}