package cvetkovic.ir;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;
import java.util.stream.Collectors;

public class LiveVariableAnalyzer {

    public static class LiveVariables {
        public Map<BasicBlock, Set<Obj>> liveIn;
        public Map<BasicBlock, Set<Obj>> liveOut;
    }

    /**
     * https://www.cs.cornell.edu/courses/cs4120/2011fa/lectures/lec21-fa11.pdf
     */
    public static LiveVariables doLivenessAnalysis(List<BasicBlock> cfg) {
        LiveVariables result = new LiveVariables();

        Map<BasicBlock, Set<Obj>> liveOut = new HashMap<>();
        Queue<BasicBlock> w = new ArrayDeque<>();

        Map<BasicBlock, Set<Obj>> notDefinedIn = new HashMap<>();
        Map<BasicBlock, Set<Obj>> usedIn = new HashMap<>();

        for (BasicBlock b : cfg) {
            //result.liveIn.put(b, new HashSet<>());
            liveOut.put(b, new HashSet<>());

            notDefinedIn.put(b, determineNotDefinedIn(cfg, b));
            usedIn.put(b, determineUsedIn(b));

            w.add(b);
        }

        throw new RuntimeException("Not yet implemented.");

        /*boolean changed = false;

        do {
            changed = false;
            for (int i = 0; i < cfg.size(); i++) {
                BasicBlock n = cfg.get(i);

                Set<Obj> oldLiveOut = new HashSet<>(liveOut.get(n));
                Set<Obj> newLiveOut = new HashSet<>();
                for (BasicBlock m : n.successors) {
                    Set<Obj> liveOutM = new HashSet<>(liveOut.get(m));
                    liveOutM.retainAll(notDefinedIn.get(m));
                    liveOutM.addAll(usedIn.get(m));

                    newLiveOut.addAll(liveOutM);
                }

                liveOut.put(n, newLiveOut);

                if (!oldLiveOut.equals(newLiveOut))
                    changed = true;
            }
        } while (changed);


        /*Set<Obj> newLiveOut = new HashSet<>();
        for (BasicBlock n_prim : n.successors)
            newLiveOut.addAll(result.liveIn.get(n_prim));
        result.liveOut.put(n, newLiveOut);

        Set<Obj> oldLiveIn = new HashSet<>(result.liveIn.get(n));
        Set<Obj> newLiveIn = new HashSet<>();

        newLiveIn.addAll(result.liveOut.get(n));
        newLiveIn.retainAll(notDefinedIn.get(n));
        newLiveIn.addAll(usedIn.get(n));
        result.liveIn.put(n, newLiveIn);

        if (!newLiveIn.equals(oldLiveIn))
            w.addAll(n.predecessors);*/

        /*result.liveOut = liveOut;

        return result;*/
    }

    private static Set<Obj> determineUsedIn(BasicBlock n) {
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

    private static Set<Obj> determineDefinedIn(BasicBlock n) {
        Set<Obj> result = new HashSet<>();

        for (Quadruple q : n.instructions)
            if (q.getResult() != null && q.getResult() instanceof QuadrupleObjVar && !((QuadrupleObjVar) q.getResult()).getObj().tempVar)
                result.add(((QuadrupleObjVar) q.getResult()).getObj());

        return result;
    }

    private static Set<Obj> determineNotDefinedIn(List<BasicBlock> cfg, BasicBlock n) {
        Set<Obj> allVars = new HashSet<>();

        for (BasicBlock b : cfg)
            allVars.addAll(b.getNonTemporaryVariables().stream().filter(p -> p.getKind() != Obj.Con).collect(Collectors.toSet()));
        allVars.removeAll(determineDefinedIn(n));

        return allVars;
    }
}
