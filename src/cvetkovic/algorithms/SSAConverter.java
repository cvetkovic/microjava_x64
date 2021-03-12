package cvetkovic.algorithms;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleIntegerConst;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadruplePhi;
import cvetkovic.misc.Config;
import cvetkovic.optimizer.Optimizer;
import cvetkovic.x64.SystemV_ABI;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contains methods needed to convert the code sequence into static-single assignment (SSA) form
 */
public class SSAConverter {

    private final DominanceAnalyzer dominanceAnalyzer;

    public SSAConverter(DominanceAnalyzer dominanceAnalyzer) {
        this.dominanceAnalyzer = dominanceAnalyzer;
    }

    /**
     * Function that does the PHI function placement
     */
    public void doPhiPlacement() {
        List<BasicBlock> basicBlocks = dominanceAnalyzer.getBasicBlocks();
        //LiveVariableAnalyzer.LiveVariables liveVariables = LiveVariableAnalyzer.doLivenessAnalysis(basicBlocks);

        Map<Obj, Set<BasicBlock>> defSites = new HashMap<>();

        for (BasicBlock n : basicBlocks) {
            for (Obj a : n.getSetOfDefinedVariables()) {
                // temp vars are already in the SSA form
                if (a.tempVar)
                    continue;

                if (defSites.containsKey(a)) {
                    defSites.get(a).add(n);
                } else {
                    Set<BasicBlock> newSet = new HashSet<>();
                    newSet.add(n);
                    defSites.put(a, newSet);
                }
            }
        }

        int phiID = 0;

        for (Obj a : defSites.keySet()) {
            Set<BasicBlock> W = defSites.get(a);
            Set<BasicBlock> A_phi = new HashSet<>();

            while (!W.isEmpty()) {
                BasicBlock n = W.iterator().next();
                Set<BasicBlock> DF_n = dominanceAnalyzer.getDominanceFrontier().get(n);

                for (BasicBlock y : DF_n) {
                    if (!A_phi.contains(y)) { // PRUNED SSA -> && liveVariables.liveIn.get(y).contains(a)) {
                        //int numberOfPredecessors = getAllPredecessors(y).size();
                        QuadruplePhi phiArgs = new QuadruplePhi(a);

                        Quadruple phi = new Quadruple(IRInstruction.STORE_PHI, phiArgs, null);
                        phi.setResult(new QuadrupleObjVar(a));
                        phi.setPhiID(phiID++);
                        // insert after the label
                        y.insertInstruction(phi);

                        A_phi.add(y);

                        //if (!y.getSetOfDefinedVariables().contains(a))
                        W.add(y);
                    }
                }

                W.remove(n);
            }
        }

        //System.out.println("Phi functions inserted where necessary.");
    }

    /**
     * Renames the variables in the whole function. Call after PHI function insertion only.
     */
    public void renameVariables() {
        Map<Obj, Integer> count = new HashMap<>();
        Map<Obj, Stack<Integer>> stack = new HashMap<>();
        Set<BasicBlock> visited = new HashSet<>();

        // initialization
        for (BasicBlock b : dominanceAnalyzer.getBasicBlocks()) {
            for (Obj var : b.allVariables) {
                if (!count.containsKey(var)) {
                    count.put(var, 0);
                    stack.put(var, new Stack<>());
                    stack.get(var).push(0);
                }
            }
        }

        // renaming starts from the entry block
        internalRenaming(dominanceAnalyzer.dominatorTreeRoot, visited, count, stack);

        // asserting algorithm correctness
        for (Stack<Integer> s : stack.values())
            assert s.peek() == 0;

        //System.out.println("Renaming has been done.");
    }

    private void internalRenaming(DominanceAnalyzer.DominatorTreeNode dominatorTreeNode, Set<BasicBlock> visited, Map<Obj, Integer> count, Map<Obj, Stack<Integer>> stack) {
        BasicBlock n = dominatorTreeNode.basicBlock;

        if (visited.contains(n))
            return;

        visited.add(n);

        // NOTE: this maps is needed to count how many pushes to the stack were done on particular Obj node
        // because the stack needs to be popped the same number of times
        Map<Obj, Integer> howManyDefinitions = new HashMap<>();

        // usages and definitions in each statement of the basic block
        for (Quadruple statement : n.instructions) {
            // dealing with usage
            if (statement.getInstruction() != IRInstruction.STORE_PHI) {
                if (statement.getArg1() != null && statement.getArg1() instanceof QuadrupleObjVar) {
                    Obj arg1 = ((QuadrupleObjVar) statement.getArg1()).getObj();
                    if (arg1.getKind() != Obj.Con && arg1.getKind() != Obj.Meth) {
                        int i = stack.get(arg1).peek();
                        statement.setSSACountArg1(i);
                    }
                }
                if (statement.getArg2() != null && statement.getArg2() instanceof QuadrupleObjVar) {
                    Obj arg2 = ((QuadrupleObjVar) statement.getArg2()).getObj();
                    if (arg2.getKind() != Obj.Con && arg2.getKind() != Obj.Meth) {
                        int i = stack.get(arg2).peek();
                        statement.setSSACountArg2(i);
                    }
                }
            }

            // dealing with definitions
            if (statement.getResult() != null && statement.getResult() instanceof QuadrupleObjVar) {
                Obj obj = ((QuadrupleObjVar) statement.getResult()).getObj();

                int i = count.get(obj) + 1;
                count.put(obj, i);
                stack.get(obj).push(i);
                statement.setSSACountResult(i);

                int howMany = howManyDefinitions.getOrDefault(obj, 0);
                howManyDefinitions.put(obj, howMany + 1);
            }
        }

        // patching the successors of a basic block with respect to the CFG
        for (BasicBlock Y : n.successors) {
            for (Quadruple statement : Y.instructions) {
                if (statement.getInstruction() != IRInstruction.STORE_PHI)
                    continue;

                Obj obj = ((QuadrupleObjVar) statement.getResult()).getObj();
                int i = stack.get(obj).peek();
                QuadruplePhi phiFunction = ((QuadruplePhi) statement.getArg1());
                phiFunction.setPhiArg(i);
            }
        }

        // renaming by traversing the dominator tree
        for (DominanceAnalyzer.DominatorTreeNode node : dominatorTreeNode.children)
            internalRenaming(node, visited, count, stack);

        // popping off stack
        for (Obj o : n.getSetOfDefinedVariables()) {
            int howManyPops = howManyDefinitions.get(o);

            for (int i = 0; i < howManyPops; i++)
                stack.get(o).pop();
        }
    }

    /**
     * Puts code back to normal form. Phi functions are replaced with STORE dest, src with
     * an instruction inserted at each of the predecessors.
     */
    public void toNormalForm() {
        BasicBlock entryBlock = dominanceAnalyzer.getBasicBlocks().stream().filter(BasicBlock::isEntryBlock).collect(Collectors.toList()).get(0);

        for (BasicBlock block : dominanceAnalyzer.getBasicBlocks()) {
            List<Quadruple> toReplace = new ArrayList<>();
            Set<Obj> phis = new HashSet<>();

            // creating worklist
            for (int i = 0; i < block.instructions.size(); i++) {
                Quadruple instruction = block.instructions.get(i);
                if (instruction.getInstruction() == IRInstruction.GEN_LABEL)
                    continue;
                else if (instruction.getInstruction() == IRInstruction.STORE_PHI)
                    toReplace.add(instruction);
                else
                    instruction.SSAToNormalForm();
            }

            for (Quadruple instruction : toReplace) {
                // maybe add reverse sort
                Obj destinationNode = ((QuadrupleObjVar) instruction.getResult()).getObj();
                Obj sourceNode = new Obj(destinationNode.getKind(), Config.prefix_phi + instruction.getPhiID(), destinationNode.getType());

                // adding STORE to predecessors
                for (BasicBlock p : block.getAllPredecessors()) {
                    // only if p defines 'sourceNode'
                    if (!p.getSetOfDefinedVariables().contains(destinationNode) && !(p.isEntryBlock() && destinationNode.parameter))
                        continue;

                    Quadruple mov = new Quadruple(IRInstruction.STORE);
                    mov.setArg1(new QuadrupleObjVar(destinationNode));
                    mov.setResult(new QuadrupleObjVar(sourceNode));

                    p.allVariables.add(sourceNode);
                    p.allVariables.add(destinationNode);
                    if (IRInstruction.isUnconditionalJumpInstruction(p.instructions.get(p.instructions.size() - 1).getInstruction()))
                        p.instructions.add(p.instructions.size() - 1, mov);
                    else if (IRInstruction.isConditionalJumpInstruction(p.instructions.get(p.instructions.size() - 1).getInstruction()))
                        p.instructions.add(p.instructions.size() - 2, mov);
                    else
                        p.instructions.add(mov);
                }

                // replacing PHI with STORE from phi_X
                Quadruple mov = new Quadruple(IRInstruction.STORE);
                mov.setArg1(new QuadrupleObjVar(sourceNode));
                mov.setResult(new QuadrupleObjVar(destinationNode));
                block.allVariables.add(sourceNode);
                block.allVariables.add(destinationNode);

                block.instructions.set(block.instructions.indexOf(instruction), mov);

                phis.add(sourceNode);
            }

            // allocate stack for _phi variables
            int oldAllocationValue = ((QuadrupleIntegerConst) entryBlock.instructions.get(1).getArg1()).getValue();
            int lastSize = Optimizer.giveAddressToTemps(phis, oldAllocationValue);

            entryBlock.instructions.get(1).setArg1(new QuadrupleIntegerConst(SystemV_ABI.alignTo16(lastSize)));
        }

        //System.out.println("Function has been put from SSA back into the normal form.");
    }
}
