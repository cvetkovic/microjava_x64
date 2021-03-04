package cvetkovic.ir;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;

public class LiveVariableAnalyzer {

    public static class LiveVariables {
        public Map<BasicBlock, Set<Obj>> liveIn;
        public Map<BasicBlock, Set<Obj>> liveOut;
    }

    /**
     * https://www.cs.cornell.edu/courses/cs4120/2011fa/lectures/lec21-fa11.pdf
     */
    public static LiveVariables doLivenessAnalysis(List<BasicBlock> basicBlocks) {
        LiveVariables result = new LiveVariables();

        result.liveIn = new HashMap<>();
        result.liveOut = new HashMap<>();
        Queue<BasicBlock> w = new ArrayDeque<>();

        for (BasicBlock b : basicBlocks) {
            result.liveIn.put(b, new HashSet<>());
            result.liveOut.put(b, new HashSet<>());

            w.add(b);
        }

        while (!w.isEmpty()) {
            BasicBlock n = w.poll();

            for (BasicBlock t : n.successors)
                result.liveOut.get(n).addAll(result.liveIn.get(t));
            Set<Obj> old = new HashSet<>(result.liveIn.get(n));

            Set<Obj> newSet = new HashSet<>(result.liveOut.get(n));
            newSet.removeAll(definedIn(n));
            newSet.addAll(usedIn(n));
            result.liveIn.put(n, newSet);

            if (!newSet.equals(old))
                w.addAll(n.predecessors);
        }

        return result;
    }

    private static Set<Obj> usedIn(BasicBlock n) {
        Set<Obj> result = new HashSet<>();

        for (Quadruple q : n.instructions) {
            if (q.getArg1() != null && q.getArg1() instanceof QuadrupleObjVar) {
                Obj arg1 = ((QuadrupleObjVar) q.getArg1()).getObj();
                if (arg1.getKind() != Obj.Con && !arg1.tempVar)
                    result.add(arg1);
            }

            if (q.getArg2() != null && q.getArg2() instanceof QuadrupleObjVar) {
                Obj arg2 = ((QuadrupleObjVar) q.getArg2()).getObj();
                if (arg2.getKind() != Obj.Con && !arg2.tempVar)
                    result.add(arg2);
            }
        }

        return result;
    }

    private static Set<Obj> definedIn(BasicBlock n) {
        Set<Obj> result = new HashSet<>();

        for (Quadruple q : n.instructions)
            if (q.getResult() != null && q.getResult() instanceof QuadrupleObjVar && !((QuadrupleObjVar)q.getResult()).getObj().tempVar)
                result.add(((QuadrupleObjVar) q.getResult()).getObj());

        return result;
    }
}
