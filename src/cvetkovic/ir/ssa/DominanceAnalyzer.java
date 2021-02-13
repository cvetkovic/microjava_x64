package cvetkovic.ir.ssa;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.optimizer.CodeSequence;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DominanceAnalyzer {

    static class DominatorTreeNode {
        final BasicBlock basicBlock;
        final List<DominatorTreeNode> children = new ArrayList<>();

        public DominatorTreeNode(BasicBlock basicBlock) {
            this.basicBlock = basicBlock;
        }
    }

    DominatorTreeNode dominatorTreeRoot;
    private final CodeSequence sequence;
    private final List<BasicBlock> basicBlocks;

    private Map<BasicBlock, Set<BasicBlock>> dominators;
    private Map<BasicBlock, BasicBlock> idoms;
    private Map<BasicBlock, Set<BasicBlock>> dominanceFrontier;

    public DominanceAnalyzer(CodeSequence sequence) {
        this.sequence = sequence;
        this.basicBlocks = sequence.basicBlocks;

        dominators = generateDominatorTree();
        idoms = generateImmediateDominators();
        dominanceFrontier = generateDominanceFrontier();
    }

    public CodeSequence getSequence() {
        return sequence;
    }

    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public Map<BasicBlock, Set<BasicBlock>> getDominators() {
        return dominators;
    }

    public Map<BasicBlock, BasicBlock> getImmediateDominators() {
        return idoms;
    }

    public Map<BasicBlock, Set<BasicBlock>> getDominanceFrontier() {
        return dominanceFrontier;
    }

    private Map<BasicBlock, Set<BasicBlock>> generateDominatorTree() {
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

        boolean changed;
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

            System.out.println("Dom(" + b.blockId + ") = { " + s.substring(0, s.length() - 2) + " }");
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

    private Map<BasicBlock, BasicBlock> generateImmediateDominators() {
        Map<BasicBlock, BasicBlock> idoms = new HashMap<>();

        // calculating immediate dominators for each basic block
        for (BasicBlock b : dominators.keySet())
            idoms.put(b, immediatelyDominates(b, dominators));

        // generating dominator tree structure
        Map<BasicBlock, DominatorTreeNode> treeNodes = new HashMap<>();
        for (BasicBlock b : basicBlocks) {
            DominatorTreeNode newNode = new DominatorTreeNode(b);
            treeNodes.put(b, newNode);

            if (b.isEntryBlock())
                dominatorTreeRoot = newNode;
        }

        for (BasicBlock p : idoms.keySet()) {
            if (idoms.get(p) != null) {
                BasicBlock parent = idoms.get(p);

                treeNodes.get(parent).children.add(treeNodes.get(p));
            }
        }

        return idoms;
    }

    /**
     * df(n) contains the first nodes reachable from n that n does not
     * dominate, on each cfg path leaving n.
     */
    private Map<BasicBlock, Set<BasicBlock>> generateDominanceFrontier() {
        Map<BasicBlock, Set<BasicBlock>> result = new HashMap<>();
        dominators.forEach((p, v) -> result.put(p, new HashSet<>()));

        // call singleDominanceFrontier in postorder on dominance tree
        Stack<DominatorTreeNode> stack = new Stack<>();
        Set<DominatorTreeNode> visited = new HashSet<>();

        DominatorTreeNode next = dominatorTreeRoot;
        stack.push(next);

        while (!stack.isEmpty()) {
            next = stack.peek();

            if (next.children.size() == 0 || visited.contains(next)) {
                DominatorTreeNode X = stack.pop();
                Set<BasicBlock> DF = singleDominanceFrontier(X, result, idoms);

                result.put(X.basicBlock, DF);
            } else {
                next.children.forEach(stack::push);
                visited.add(next);
            }
        }

        for (BasicBlock b : result.keySet()) {
            StringBuilder s = new StringBuilder();

            if (result.get(b).size() != 0)
                result.get(b).forEach(p -> s.append(p.blockId).append(", "));
            else
                s.append("null, ");

            System.out.println("DF(" + b.blockId + ") = { " + s.substring(0, s.length() - 2) + " }");
        }

        return result;
    }

    private Set<BasicBlock> singleDominanceFrontier(DominatorTreeNode node, Map<BasicBlock, Set<BasicBlock>> DF, Map<BasicBlock, BasicBlock> idoms) {
        Set<BasicBlock> frontier = new HashSet<>();

        // local (done on CFG)
        for (BasicBlock Y : node.basicBlock.successor)
            if (idoms.get(Y) != node.basicBlock)
                frontier.add(Y);

        // determining children of the dominator tree
        List<BasicBlock> dominatorTreeChildren = new ArrayList<>();
        node.children.forEach(p -> dominatorTreeChildren.add(p.basicBlock));
        // up (done on dominator tree)
        for (BasicBlock Z : dominatorTreeChildren)
            for (BasicBlock Y : DF.get(Z))
                if (idoms.get(Y) != node.basicBlock)
                    frontier.add(Y);

        return frontier;
    }

    public static void dumpCFG(String path, List<BasicBlock> basicBlocks) {
        (new File(path)).getParentFile().mkdir();
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

    public static void dumpDominatorTree(String path, Map<BasicBlock, BasicBlock> idoms) {
        (new File(path)).getParentFile().mkdir();
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
