package cvetkovic.ir.optimizations;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleLabel;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadrupleVariable;
import cvetkovic.misc.Config;
import cvetkovic.structures.SymbolTable;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;
import java.util.stream.Collectors;

public class BasicBlock {

    //////////////////////////////////////////////////////////////////////////////////
    // CLASS FIELDS
    //////////////////////////////////////////////////////////////////////////////////

    private static int blockCounter = 0;
    public int blockId = blockCounter++;

    public List<BasicBlock> predecessors = new ArrayList<>();
    public List<BasicBlock> successors = new ArrayList<>(2);

    public List<Quadruple> instructions = new ArrayList<>();

    public Collection<Obj> allVariables;

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
    }

    public Set<Obj> getNonTemporaryVariables() {
        return allVariables.stream().filter(p -> !p.tempVar).collect(Collectors.toSet());
    }

    public Set<Obj> getTemporaryVariables() {
        return allVariables.stream().filter(p -> p.tempVar).collect(Collectors.toSet());
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

    public Quadruple getLastInstruction() {
        if (instructions.size() == 0)
            throw new RuntimeException("There are no instructions in the block.");

        return instructions.get(instructions.size() - 1);
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

            if (IRInstruction.isJumpInstruction(quadruple.getInstruction())) {
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
            else if (IRInstruction.isJumpInstruction(lastInstruction.getInstruction()) &&
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
                b.successors.add(firstInstruction.get(i));
                firstInstruction.get(i).predecessors.add(b);
            }
        }

        return makeCanonicalForm(basicBlocks);
    }

    private static int canonicalFormLabelGenerator = 0;
    private static int canonicalFormVarGenerator = 0;

    /**
     * This method inserts CMP, JMP instructions where necessary in order to make code generation
     * be easier. After this method code for basic blocks can be generated and written into a file
     * in arbitrary order.
     * <p>
     * This method also removes duplicated labels
     */
    private static List<BasicBlock> makeCanonicalForm(List<BasicBlock> basicBlocks) {
        // removing duplicated GEN_LABEL
        for (BasicBlock block : basicBlocks) {
            String redirectTo;
            if (block.instructions.get(0).getInstruction() == IRInstruction.GEN_LABEL)
                redirectTo = ((QuadrupleLabel) block.instructions.get(0).getArg1()).getLabelName();
            else
                continue;

            Set<Quadruple> toRemove = new HashSet<>();
            for (int i = 1; i < block.instructions.size(); i++) {
                if (block.instructions.get(i).getInstruction() != IRInstruction.GEN_LABEL)
                    break;

                String redirectFrom = ((QuadrupleLabel) block.instructions.get(i).getArg1()).getLabelName();
                for (BasicBlock user : basicBlocks) {
                    for (Quadruple q : user.instructions) {
                        if (q.getArg1() != null && q.getArg1() instanceof QuadrupleLabel && ((QuadrupleLabel) q.getArg1()).getLabelName().equals(redirectFrom))
                            q.setArg1(new QuadrupleLabel(redirectTo));
                        if (q.getArg2() != null && q.getArg2() instanceof QuadrupleLabel && ((QuadrupleLabel) q.getArg2()).getLabelName().equals(redirectFrom))
                            q.setArg2(new QuadrupleLabel(redirectTo));
                        if (q.getResult() != null && q.getResult() instanceof QuadrupleLabel && ((QuadrupleLabel) q.getResult()).getLabelName().equals(redirectFrom))
                            q.setResult(new QuadrupleLabel(redirectTo));
                    }
                }

                toRemove.add(block.instructions.get(i));
            }

            block.instructions.removeAll(toRemove);
        }

        // fixing jumps
        for (BasicBlock block : basicBlocks) {
            Quadruple lastInstruction = block.instructions.get(block.instructions.size() - 1);

            // if basic block just falls through then add explicit JMP
            if (!IRInstruction.isJumpInstruction(lastInstruction.getInstruction()) && block.hasSuccessors()) {
                BasicBlock followingBlock = block.successors.get(0);

                Quadruple firstTargetInstruction = followingBlock.instructions.get(0);
                QuadrupleLabel targetJump;
                if (firstTargetInstruction.getInstruction() == IRInstruction.GEN_LABEL)
                    targetJump = new QuadrupleLabel(((QuadrupleLabel) firstTargetInstruction.getArg1()).getLabelName());
                else {
                    String label = "CFLG_" + canonicalFormLabelGenerator++;
                    followingBlock.instructions.add(0, new Quadruple(IRInstruction.GEN_LABEL, new QuadrupleLabel(label), null));
                    targetJump = new QuadrupleLabel(label);
                }
                Quadruple newJMP = new Quadruple(IRInstruction.JMP);
                newJMP.setResult(targetJump);
                block.instructions.add(newJMP);
            }
            // conditional branches require additional instruction insertion
            else if (IRInstruction.isConditionalJumpInstruction(lastInstruction.getInstruction())) {
                Quadruple cmp = new Quadruple(IRInstruction.CMP);
                cmp.setArg1(lastInstruction.getArg1());
                cmp.setArg2(lastInstruction.getArg2());

                Obj cmpResult = new Obj(Obj.Var, Config.compare_tmp + canonicalFormVarGenerator++, SymbolTable.intType);
                cmpResult.tempVar = true;
                cmp.setResult(new QuadrupleObjVar(cmpResult));
                block.instructions.add(block.instructions.size() - 1, cmp);
                block.allVariables.add(cmpResult);

                BasicBlock successor1 = block.successors.get(0);
                BasicBlock successor2 = block.successors.get(1);
                BasicBlock addInstructionIn = successor1;

                String trueDestination = ((QuadrupleLabel) lastInstruction.getResult()).getLabelName();
                if (successor1.instructions.get(0).getInstruction() == IRInstruction.GEN_LABEL &&
                        ((QuadrupleLabel) successor1.instructions.get(0).getArg1()).getLabelName().equals(trueDestination))
                    addInstructionIn = successor2;

                Quadruple firstTargetInstruction = addInstructionIn.instructions.get(0);
                QuadrupleLabel targetJump;
                if (firstTargetInstruction.getInstruction() == IRInstruction.GEN_LABEL)
                    targetJump = new QuadrupleLabel(((QuadrupleLabel) firstTargetInstruction.getArg1()).getLabelName());
                else {
                    String label = "CFLG_" + canonicalFormLabelGenerator++;
                    addInstructionIn.instructions.add(0, new Quadruple(IRInstruction.GEN_LABEL, new QuadrupleLabel(label), null));
                    targetJump = new QuadrupleLabel(label);
                }

                lastInstruction.setArg1(new QuadrupleObjVar(cmpResult));
                lastInstruction.setArg2(lastInstruction.getResult());
                lastInstruction.setResult(targetJump);
            }

        }

        return basicBlocks;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // FUNCTION MEMBERS
    //////////////////////////////////////////////////////////////////////////////////

    private boolean hasSuccessors() {
        return successors.size() > 0;
    }

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
        return instructions.stream().filter(p -> p.getInstruction() == IRInstruction.ENTER).count() == 1;
    }

    public boolean isExitBlock() {
        return successors.size() == 0;
    }

    @Override
    public String toString() {
        final StringBuilder p = new StringBuilder();
        p.append("(");
        predecessors.forEach(x -> p.append(x.blockId).append(", "));
        p.append(")");

        final StringBuilder s = new StringBuilder();
        s.append("(");
        successors.forEach(x -> s.append(x.blockId).append(", "));
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
                case STORE_PHI:
                case MALLOC:
                case ALOAD:
                case ASTORE:
                case GET_PTR:
                    // I/O
                case SCANF:
                    result.add(((QuadrupleObjVar) q.getResult()).getObj());
                    break;
                // functions
                case CALL:
                case INVOKE_VIRTUAL:
                    if (q.getResult() != null)
                        result.add(((QuadrupleObjVar) q.getResult()).getObj());
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    public boolean isEmpty() {
        return instructions.size() == 2 &&
                instructions.get(0).getInstruction() == IRInstruction.GEN_LABEL &&
                instructions.get(1).getInstruction() == IRInstruction.JMP;
    }

    public Set<BasicBlock> getAllPredecessors() {
        Set<BasicBlock> result = new HashSet<>();

        Stack<BasicBlock> stack = new Stack<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            BasicBlock current = stack.pop();

            for (BasicBlock child : current.predecessors)
                if (!result.contains(current))
                    stack.push(child);

            result.add(current);
        }

        // initial is not a successor
        result.remove(this);

        return result;
    }

    public Set<BasicBlock> getAllSuccessors() {
        Set<BasicBlock> result = new HashSet<>();

        Stack<BasicBlock> stack = new Stack<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            BasicBlock current = stack.pop();

            for (BasicBlock child : current.successors)
                if (!result.contains(current))
                    stack.push(child);

            result.add(current);
        }

        // initial is not a successor
        result.remove(this);

        return result;
    }

    public String getLabelName() {
        if (instructions.size() == 0)
            throw new RuntimeException("Invalid basic block to get label name.");

        Quadruple firstInstruction = instructions.get(0);
        if (firstInstruction.getInstruction() != IRInstruction.GEN_LABEL)
            throw new RuntimeException("Invalid basic block to get label name (first instruction is not GEN_LABEL).");

        return ((QuadrupleLabel) firstInstruction.getArg1()).getLabelName();
    }
}