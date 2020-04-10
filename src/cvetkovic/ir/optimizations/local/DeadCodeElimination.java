package cvetkovic.ir.optimizations.local;

import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.optimizer.OptimizerPass;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;

public class DeadCodeElimination implements OptimizerPass {

    private List<Quadruple> instructions;

    public DeadCodeElimination(List<Quadruple> instructions) {
        this.instructions = instructions;

        mark();
    }

    private void mark() {
        List<Quadruple> worklist = new ArrayList<>();
        Set<Quadruple> marked = new HashSet<>();

        for (Quadruple q : instructions) {
            marked.remove(q);
            if (isCritial(q)) {
                marked.add(q);
                worklist.add(q);
            }
        }

        Map<Obj, Quadruple> definitions = new HashMap<>();

        while (worklist.size() > 0) {
            Quadruple q = worklist.get(0);
            definitions.put(((QuadrupleObjVar) q.getResult()).getObj(), q);

            if (q.getArg1() != null &&
                    q.getArg1() instanceof QuadrupleObjVar &&
                    !marked.contains(((QuadrupleObjVar) q.getArg1()).getObj())) {
                Quadruple tmp = definitions.get(((QuadrupleObjVar) q.getArg1()).getObj());

                marked.add(tmp);
                worklist.add(tmp);
            }

            if (q.getArg2() != null &&
                    q.getArg2() instanceof QuadrupleObjVar &&
                    !marked.contains(((QuadrupleObjVar) q.getArg2()).getObj())) {
                Quadruple tmp = definitions.get(((QuadrupleObjVar) q.getArg2()).getObj());

                marked.add(tmp);
                worklist.add(tmp);
            }

            /*List<BasicBlock> rdfBlocks;
            for (BasicBlock block : rdfBlocks) {
                Quadruple branchInstruction;

                if (branchInstruction != null && !marked.contains(branchInstruction)) {
                    marked.add(branchInstruction);
                    worklist.add(branchInstruction);
                }
            }*/
        }
    }

    private boolean isCritial(Quadruple quadruple) {
        return false;
    }

    @Override
    public void optimize() {

    }

    @Override
    public void finalizePass() {

    }
}
