package cvetkovic.ir.ssa;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.misc.Config;
import cvetkovic.optimizer.CodeSequence;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DominanceAnalyzer {

    public static class DominatorTreeNode {
        public final BasicBlock basicBlock;
        public final List<DominatorTreeNode> children = new ArrayList<>();

        public DominatorTreeNode(BasicBlock basicBlock) {
            this.basicBlock = basicBlock;
        }
    }

    ///////////////////////////////
    // MEMBERS
    ///////////////////////////////

    DominatorTreeNode dominatorTreeRoot;
    DominatorTreeNode reverseDominatorTreeRoot;
    private final CodeSequence sequence;

    private final Map<BasicBlock, Set<BasicBlock>> dominators;
    private final Map<BasicBlock, BasicBlock> idoms;
    private final Map<BasicBlock, Set<BasicBlock>> dominanceFrontier;

    private final Map<BasicBlock, Set<BasicBlock>> reverseDominators;
    private final Map<BasicBlock, BasicBlock> reverseIdoms;
    private final Map<BasicBlock, Set<BasicBlock>> reverseDominanceFrontier;

    private final Map<BasicBlock, Set<BasicBlock>> controlDependence;
    private final List<BasicBlock.Tuple<BasicBlock, Set<BasicBlock>>> naturalLoops;

    ///////////////////////////////
    // CONSTRUCTOR
    ///////////////////////////////

    public DominanceAnalyzer(CodeSequence sequence) {
        this.sequence = sequence;

        List<BasicBlock> CFG = getBasicBlocks();
        dominators = generateDominatorTree(CFG, false);
        idoms = generateImmediateDominators(CFG, dominators, false);
        dominanceFrontier = generateDominanceFrontier(dominators, idoms, false);

        reverseDominators = generateDominatorTree(CFG, true);
        reverseIdoms = generateImmediateDominators(CFG, reverseDominators, true);
        reverseDominanceFrontier = generateDominanceFrontier(reverseDominators, reverseIdoms, true);

        controlDependence = determineControlDependence(CFG, reverseDominanceFrontier);
        naturalLoops = determineNaturalLoops();
    }

    ///////////////////////////////
    // GETTERS
    ///////////////////////////////

    public CodeSequence getSequence() {
        return sequence;
    }

    public List<BasicBlock> getBasicBlocks() {
        return sequence.basicBlocks;
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

    public Map<BasicBlock, Set<BasicBlock>> getReverseDominators() {
        return reverseDominators;
    }

    public Map<BasicBlock, BasicBlock> getReverseImmediateDominators() {
        return reverseIdoms;
    }

    public Map<BasicBlock, Set<BasicBlock>> getReverseDominanceFrontier() {
        return reverseDominanceFrontier;
    }

    public Map<BasicBlock, Set<BasicBlock>> getControlDependencies() {
        return controlDependence;
    }

    public List<BasicBlock.Tuple<BasicBlock, Set<BasicBlock>>> getNaturalLoops() {
        return naturalLoops;
    }

    ///////////////////////////////
    ///////////////////////////////
    ///////////////////////////////

    private Map<BasicBlock, Set<BasicBlock>> generateDominatorTree(List<BasicBlock> basicBlocks, boolean reverse) {
        Map<BasicBlock, Set<BasicBlock>> dominators = new HashMap<>();
        BasicBlock initialBlock = basicBlocks.stream().filter((reverse) ? BasicBlock::isExitBlock : BasicBlock::isEntryBlock).collect(Collectors.toList()).get(0);

        // dominator of the entry block is the entry block itself
        dominators.put(initialBlock, Collections.singleton(initialBlock));

        // for all other nodes excluding the entry block set all nodes as the dominators
        for (BasicBlock b : basicBlocks) {
            if (b == initialBlock)
                continue;

            dominators.put(b, new HashSet<>(basicBlocks));
        }

        boolean changed;
        do {
            changed = false;

            for (BasicBlock b : basicBlocks) {
                if (b == initialBlock)
                    continue;

                Set<BasicBlock> old = dominators.get(b);

                List<Set<BasicBlock>> setOfPredecessors = new ArrayList<>();
                for (BasicBlock predecessor : (reverse ? b.successors : b.predecessors))
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
            if ((reverse ? !b.isExitBlock() : !b.isEntryBlock()))
                dominators.get(b).forEach(p -> s.append(p.blockId).append(", "));
            else
                s.append("entry, ");

            if (Config.printDominatorRelations)
                System.out.println((reverse ? "R" : "") + "Dom(" + b.blockId + ") = { " + s.substring(0, s.length() - 2) + " }");
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

    private Map<BasicBlock, BasicBlock> generateImmediateDominators(List<BasicBlock> basicBlocks, Map<BasicBlock, Set<BasicBlock>> dominators, boolean reverse) {
        Map<BasicBlock, BasicBlock> idoms = new HashMap<>();

        // calculating immediate dominators for each basic block
        for (BasicBlock b : dominators.keySet())
            idoms.put(b, immediatelyDominates(b, dominators));

        // generating dominator tree structure
        Map<BasicBlock, DominatorTreeNode> treeNodes = new HashMap<>();
        for (BasicBlock b : basicBlocks) {
            DominatorTreeNode newNode = new DominatorTreeNode(b);
            treeNodes.put(b, newNode);

            if ((reverse ? b.isExitBlock() : b.isEntryBlock())) {
                if (reverse)
                    reverseDominatorTreeRoot = newNode;
                else
                    dominatorTreeRoot = newNode;
            }
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
    private Map<BasicBlock, Set<BasicBlock>> generateDominanceFrontier(Map<BasicBlock, Set<BasicBlock>> dominators, Map<BasicBlock, BasicBlock> idoms, boolean reverse) {
        Map<BasicBlock, Set<BasicBlock>> result = new HashMap<>();
        dominators.forEach((p, v) -> result.put(p, new HashSet<>()));

        // call singleDominanceFrontier in postorder on dominance tree
        Stack<DominatorTreeNode> stack = new Stack<>();
        Set<DominatorTreeNode> visited = new HashSet<>();

        DominatorTreeNode next = (reverse ? reverseDominatorTreeRoot : dominatorTreeRoot);
        stack.push(next);

        while (!stack.isEmpty()) {
            next = stack.peek();

            if (next.children.size() == 0 || visited.contains(next)) {
                DominatorTreeNode X = stack.pop();
                Set<BasicBlock> DF = singleDominanceFrontier(X, result, idoms, reverse);

                result.put(X.basicBlock, DF);
            } else {
                next.children.forEach(stack::push);
                visited.add(next);
            }
        }

        if (Config.printDominatorRelations) {
            for (BasicBlock b : result.keySet()) {
                StringBuilder s = new StringBuilder();

                if (result.get(b).size() != 0)
                    result.get(b).forEach(p -> s.append(p.blockId).append(", "));
                else
                    s.append("null, ");

                System.out.println((reverse ? "R" : "") + "DF(" + b.blockId + ") = { " + s.substring(0, s.length() - 2) + " }");
            }
        }

        return result;
    }

    private Set<BasicBlock> singleDominanceFrontier(DominatorTreeNode node, Map<BasicBlock, Set<BasicBlock>> DF, Map<BasicBlock, BasicBlock> idoms, boolean reverse) {
        Set<BasicBlock> frontier = new HashSet<>();

        // local (done on CFG)
        for (BasicBlock Y : (reverse ? node.basicBlock.predecessors : node.basicBlock.successors))
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

    /**
     * Node Y is control-dependent on B iff B ∈ DF(Y) in RCFG.
     */
    public Map<BasicBlock, Set<BasicBlock>> determineControlDependence(List<BasicBlock> reverseCFG, Map<BasicBlock, Set<BasicBlock>> reverseDF) {
        Map<BasicBlock, Set<BasicBlock>> result = new HashMap<>();

        for (BasicBlock b : reverseCFG) {
            result.put(b, new HashSet<>());

            for (BasicBlock y : reverseDF.keySet())
                if (reverseDF.get(y).contains(b))
                    result.get(b).add(y);
        }

        if (Config.printDominatorRelations) {
            for (BasicBlock b : result.keySet()) {
                StringBuilder s = new StringBuilder();

                if (result.get(b).size() != 0)
                    result.get(b).forEach(p -> s.append(p.blockId).append(", "));
                else
                    s.append("null, ");

                System.out.println(b.blockId + " is control dependent on { " + s.substring(0, s.length() - 2) + " }");
            }
        }

        return result;
    }

    /**
     * http://www.cs.cmu.edu/afs/cs/academic/class/15745-f03/public/lectures/L7_handouts.pdf
     */
    public List<BasicBlock.Tuple<BasicBlock, Set<BasicBlock>>> determineNaturalLoops() {
        List<BasicBlock.Tuple<BasicBlock, Set<BasicBlock>>> loops = new ArrayList<>();

        /*
        Three steps:
        - finding dominators
        - finding back edges (t -> h such that h dom t)
        - detecting basic blocks that constitute loops
         */

        // DFS of the CFG
        List<BasicBlock.Tuple<BasicBlock, BasicBlock>> backEdges = new ArrayList<>();
        Stack<BasicBlock> dfsStack = new Stack<>();
        Set<BasicBlock> visited = new HashSet<>();
        dfsStack.push(dominatorTreeRoot.basicBlock);

        while (!dfsStack.isEmpty()) {
            BasicBlock t = dfsStack.pop();
            visited.add(t);

            for (BasicBlock h : t.successors) {
                // traverse children
                if (!visited.contains(h) && !dfsStack.contains(h))
                    dfsStack.push(h);

                // check for back edge condition
                if (dominators.get(t).contains(h))
                    backEdges.add(new BasicBlock.Tuple<>(t, h));
            }
        }

        if (Config.printNaturalLoopsInfo) {
            System.out.println("Back edges in " + sequence.function.getName() + ":");
            backEdges.forEach(p -> System.out.println("\t" + p.u.blockId + " -> " + p.v.blockId));
        }

        // detecting constitutive blocks
        for (BasicBlock.Tuple<BasicBlock, BasicBlock> backEdge : backEdges) {
            BasicBlock n = backEdge.u;
            BasicBlock d = backEdge.v;

            Set<BasicBlock> loop = new HashSet<>();
            // the following ensures ignorance of loop header's predecessors
            loop.add(d);

            Stack<BasicBlock> stack = new Stack<>();
            stack.push(n);
            loop.add(n);

            while (!stack.isEmpty()) {
                BasicBlock m = stack.pop();

                for (BasicBlock p : m.predecessors) {
                    if (!loop.contains(p)) {
                        loop.add(p);
                        stack.push(p);
                    }
                }
            }

            loops.add(new BasicBlock.Tuple<>(d, loop));
        }


        if (Config.printNaturalLoopsInfo) {
            if (loops.size() > 0)
                System.out.println("Natural loops detected in " + sequence.function.getName() + ":");

            for (BasicBlock.Tuple<BasicBlock, Set<BasicBlock>> loop : loops) {
                StringBuilder sb = new StringBuilder();
                loop.v.forEach(p -> sb.append(p.blockId).append(", "));
                String members = sb.substring(0, sb.length() - 2);
                boolean subset = false;
                int subsetHeaderIndex = -1;

            /* Nesting is checked by testing whether the set of nodes of a
               loop A is a subset of the set of nodes of another loop B */
                for (BasicBlock.Tuple<BasicBlock, Set<BasicBlock>> parent : loops) {
                    if (parent.v.containsAll(loop.v) && loop != parent) {
                        subset = true;
                        subsetHeaderIndex = parent.u.blockId;
                    }
                }

                System.out.println("\tHeader: " + loop.u.blockId +
                        ", Nested: " + (subset ? "True (" + subsetHeaderIndex + ")" : "False") +
                        ", Members: { " + members + " }");
            }
        }

        return loops;
    }

    ///////////////////////////////
    // DUMPING
    ///////////////////////////////

    public static void dumpCFG(String path, String reversePath, List<BasicBlock> basicBlocks) {
        (new File(path)).getParentFile().mkdir();
        (new File(reversePath)).getParentFile().mkdir();

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path)));
             PrintWriter reverseWriter = new PrintWriter(new BufferedWriter(new FileWriter(reversePath)))) {
            writer.println("digraph G {");
            writer.println("node [ shape = rect ]");
            reverseWriter.println("digraph G {");
            reverseWriter.println("node [ shape = rect ]");

            for (BasicBlock p : basicBlocks) {
                StringBuilder label = new StringBuilder();

                label.append("ID = ").append(p.blockId).append("\n\n");

                p.instructions.forEach(q -> label.append(q.getNonformattedOutput()).append("\n"));

                writer.println(p.blockId + " [ label = " + '"' + label.toString() + '"' + " ]");
                reverseWriter.println(p.blockId + " [ label = " + '"' + label.toString() + '"' + " ]");

                p.successors.forEach(s -> writer.println(p.blockId + " -> " + s.blockId));
                p.successors.forEach(s -> reverseWriter.println(s.blockId + " -> " + p.blockId));
            }

            writer.println("}");
            reverseWriter.println("}");
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
