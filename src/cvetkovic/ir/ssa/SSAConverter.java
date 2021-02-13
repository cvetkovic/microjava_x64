package cvetkovic.ir.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.LiveVariableAnalyzer;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadruplePhi;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;

/**
 * Contains methods needed to convert the code sequence into static-single assignment (SSA) form
 */
public class SSAConverter {

    private DominanceAnalyzer dominanceAnalyzer;

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
                        QuadruplePhi phiArgs = new QuadruplePhi(a, y.predecessor.size());

                        Quadruple phi = new Quadruple(IRInstruction.STORE_PHI, phiArgs, null);
                        phi.setResult(new QuadrupleObjVar(a));
                        phi.setPhiID(phiID++);
                        // insert after the label
                        y.insertInstruction(phi);

                        A_phi.add(y);

                        if (!y.getSetOfDefinedVariables().contains(a))
                            W.add(y);
                    }
                }

                W.remove(n);
            }
        }

        System.out.println("Phi functions inserted where necessary.");
    }

    /**
     * Renames the variables in the whole function. Call after PHI function insertion only.
     */
    public void renameVariables() {
        Map<Obj, Integer> count = new HashMap<>();
        Map<Obj, Stack<Integer>> stack = new HashMap<>();

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
        internalRenaming(dominanceAnalyzer.dominatorTreeRoot, count, stack);

        System.out.println("Renaming has been done.");
    }

    private void internalRenaming(DominanceAnalyzer.DominatorTreeNode dominatorTreeNode, Map<Obj, Integer> count, Map<Obj, Stack<Integer>> stack) {
        BasicBlock n = dominatorTreeNode.basicBlock;

        // usages and definitions in each statement of the basic block
        for (Quadruple statement : n.instructions) {
            // dealing with usage
            if (statement.getInstruction() != IRInstruction.STORE_PHI) {
                if (statement.getArg1() != null && statement.getArg1() instanceof QuadrupleObjVar) {
                    Obj arg1 = ((QuadrupleObjVar) statement.getArg1()).getObj();
                    if (arg1.getKind() != Obj.Con) {
                        int i = stack.get(arg1).peek();
                        statement.setSSACountArg1(i);
                    }
                }
                if (statement.getArg2() != null && statement.getArg2() instanceof QuadrupleObjVar) {
                    Obj arg2 = ((QuadrupleObjVar) statement.getArg2()).getObj();
                    if (arg2.getKind() != Obj.Con) {
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
            }
        }

        // patching the successors of CFG
        for (BasicBlock Y : n.successor) {
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
            internalRenaming(node, count, stack);

        // popping off stack
        for (Obj o : n.getSetOfDefinedVariables())
            stack.get(o).pop();
    }

    /**
     * Puts code back to normal form. Phi functions are replaced with STORE dest, src with
     * an instruction inserted at each of the predecessors.
     */
    public void toNormalForm() {
        for (BasicBlock block : dominanceAnalyzer.getBasicBlocks()) {
            List<Quadruple> toReplace = new ArrayList<>();

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
                Obj sourceNode = new Obj(destinationNode.getKind(), "_phi" + instruction.getPhiID(), destinationNode.getType());

                // adding STORE to predecessors
                for (BasicBlock p : block.predecessor) {
                    Quadruple mov = new Quadruple(IRInstruction.STORE);
                    mov.setArg1(new QuadrupleObjVar(destinationNode));
                    mov.setResult(new QuadrupleObjVar(sourceNode));

                    p.instructions.add(mov);
                }

                // replacing PHI with STORE from phi_X
                Quadruple mov = new Quadruple(IRInstruction.STORE);
                mov.setArg1(new QuadrupleObjVar(sourceNode));
                mov.setResult(new QuadrupleObjVar(destinationNode));

                block.instructions.set(block.instructions.indexOf(instruction), mov);
            }
        }

        System.out.println("Function has been put from SSA back into the normal form.");
    }
}
