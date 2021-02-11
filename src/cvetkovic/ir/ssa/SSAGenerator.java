package cvetkovic.ir.ssa;

import cvetkovic.ir.optimizations.BasicBlock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class SSAGenerator {

    public SSAGenerator(List<BasicBlock> basicBlocks) {
        Map<BasicBlock, Set<BasicBlock>> dominators = generateDominatorTree(basicBlocks);
        Map<BasicBlock, BasicBlock> idoms = generateImmediateDominators(dominators);

        dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\cfg.dot", basicBlocks);
        dumpDominatorTree("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\dominator_tree.dot", dominators, idoms);
    }

    private Map<BasicBlock, Set<BasicBlock>> generateDominatorTree(List<BasicBlock> basicBlocks) {
        Map<BasicBlock, Set<BasicBlock>> dominators = new HashMap<>();
        BasicBlock entryBlock = basicBlocks.stream().filter(BasicBlock::isEntryBlock).collect(Collectors.toList()).get(0);

        // dominator of the entry block is the entry block itself
        dominators.put(entryBlock, Collections.singleton(entryBlock));

        // for all other nodes excluding the entry block set all nodes as the dominators
        for (BasicBlock b : basicBlocks) {
            if (b == entryBlock)
                continue;

            dominators.put(b, new HashSet<>(basicBlocks));
        }

        boolean changed = true;
        do {
            changed = false;

            for (BasicBlock b : basicBlocks) {
                if (b == entryBlock)
                    continue;

                Set<BasicBlock> old = dominators.get(b);

                List<Set<BasicBlock>> setOfPredecessors = new ArrayList<>();
                for (BasicBlock predecessor : b.predecessor)
                    setOfPredecessors.add(dominators.get(predecessor));
                Set<BasicBlock> tmp = new HashSet<>(setOfPredecessors.get(0));
                for (Set<BasicBlock> toIntersectWith : setOfPredecessors)
                    tmp.retainAll(toIntersectWith);
                tmp.add(b);

                dominators.put(b, tmp);

                if (!changed && !tmp.equals(old))
                    changed = true;
            }
        } while (changed);

        for (BasicBlock b : dominators.keySet()) {
            StringBuilder s = new StringBuilder();
            if (!b.isEntryBlock())
                dominators.get(b).forEach(p -> s.append(p.blockId).append(", "));
            else
                s.append("entry, ");

            System.out.println("Dom(" + b.blockId + ") = { " + s.toString().substring(0, s.length() - 2) + " }");
        }

        return dominators;
    }

    /**
     * M strictly dominates N if and only if (M dom N) and M != N
     * <p>
     * NOTE: removes itself from its own dominator set
     */
    private boolean strictlyDominates(BasicBlock M, BasicBlock N, Map<BasicBlock, Set<BasicBlock>> dominators) {
        return dominators.get(N).contains(M) && (M != N);
    }

    private boolean implication(boolean a, boolean b) {
        if (a)
            return b;
        else
            return true;
    }

    /**
     * M immediately dominates N if and only if (M sdom N) and for each P (P sdom N) => (P dom M)
     * <p>
     * NOTE: calculates nearest dominator
     * https://www.cs.purdue.edu/homes/hosking/502/notes/14-dep.pdf
     */
    private BasicBlock immediatelyDominates(BasicBlock N, Map<BasicBlock, Set<BasicBlock>> dominators) {
        for (BasicBlock M : dominators.get(N)) {
            boolean leftHandSide = strictlyDominates(M, N, dominators);

            if (!leftHandSide)
                continue;

            boolean dominatesOverAll = true;
            for (BasicBlock P : dominators.get(N)) {
                boolean term1 = strictlyDominates(P, N, dominators);
                boolean term2 = dominators.get(M).contains(P);

                if (!implication(term1, term2))
                    dominatesOverAll = false;
            }

            if (dominatesOverAll)
                return M;
        }

        return null;
    }

    private Map<BasicBlock, BasicBlock> generateImmediateDominators(Map<BasicBlock, Set<BasicBlock>> dominators) {
        Map<BasicBlock, BasicBlock> idoms = new HashMap<>();

        for (BasicBlock b : dominators.keySet())
            idoms.put(b, immediatelyDominates(b, dominators));

        return idoms;
    }

    public static void dumpCFG(String path, List<BasicBlock> basicBlocks) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path)))) {
            writer.println("digraph G {");
            writer.println("node [ shape = rect ]");

            for (BasicBlock p : basicBlocks) {
                StringBuilder label = new StringBuilder();

                label.append("ID = ").append(p.blockId).append("\n\n");

                p.instructions.forEach(q -> label.append(q.getNonformattedOutput()).append("\n"));
                writer.println(p.blockId + " [ label = " + '"' + label.toString() + '"' + " ]");

                p.successor.forEach(s -> writer.println(p.blockId + " -> " + s.blockId));
            }

            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dumpDominatorTree(String path, Map<BasicBlock, Set<BasicBlock>> dominators, Map<BasicBlock, BasicBlock> idoms) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path)))) {
            writer.println("digraph G {");

            for (BasicBlock p : idoms.keySet())
                if (idoms.get(p) != null)
                    writer.println(idoms.get(p).blockId + " -> " + p.blockId);

            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
