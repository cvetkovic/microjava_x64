package cvetkovic.ir.ssa;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.ir.quadruple.arguments.QuadruplePhi;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;

public class SSAConverter {

    private DominanceAnalyzer dominanceAnalyzer;

    public SSAConverter(DominanceAnalyzer dominanceAnalyzer) {
        this.dominanceAnalyzer = dominanceAnalyzer;
    }

    public void doPhiPlacement() {
        List<BasicBlock> basicBlocks = dominanceAnalyzer.getBasicBlocks();

        Map<Obj, Set<BasicBlock>> defSites = new HashMap<>();

        for (BasicBlock n : basicBlocks) {
            for (Obj a : n.getSetOfDefinedVariables()) {
                if (defSites.containsKey(a)) {
                    defSites.get(a).add(n);
                } else {
                    Set<BasicBlock> newSet = new HashSet<>();
                    newSet.add(n);
                    defSites.put(a, newSet);
                }
            }
        }

        for (Obj a : defSites.keySet()) {
            Set<BasicBlock> W = defSites.get(a);
            Set<BasicBlock> A_phi = new HashSet<>();

            while (!W.isEmpty()) {
                BasicBlock n = W.iterator().next();
                Set<BasicBlock> DF_n = dominanceAnalyzer.getDominanceFrontier().get(n);

                for (BasicBlock y : DF_n) {
                    if (!A_phi.contains(y)) {
                        QuadruplePhi phiArgs = new QuadruplePhi(y.predecessor.size());
                        for (int i = 0; i < y.predecessor.size(); i++)
                            phiArgs.setPhiArg(i, a);

                        Quadruple phi = new Quadruple(IRInstruction.STORE_PHI, phiArgs, null);
                        phi.setResult(new QuadrupleObjVar(a));
                        y.instructions.add(0, phi);

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

    public void renameVariables() {

    }
}
