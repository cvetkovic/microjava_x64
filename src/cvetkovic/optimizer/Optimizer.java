package cvetkovic.optimizer;

import cvetkovic.ir.BasicBlock;
import cvetkovic.algorithms.DominanceAnalyzer;
import cvetkovic.algorithms.SSAConverter;
import cvetkovic.ir.CodeSequence;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleIntegerConst;
import cvetkovic.misc.Config;
import cvetkovic.optimizer.passes.CFGCleaner;
import cvetkovic.optimizer.passes.FunctionInlining;
import cvetkovic.optimizer.passes.OptimizerPass;
import cvetkovic.optimizer.passes.ValueNumbering;
import cvetkovic.x64.SystemV_ABI;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;

public class Optimizer {

    private List<OptimizerPass> optimizationList = new ArrayList<>();
    protected List<CodeSequence> codeSequenceList = new ArrayList<>();

    protected Set<Obj> globalVariables;

    public Optimizer(List<List<Quadruple>> code, List<Obj> functions, Set<Obj> globalVariables) {
        int i = 0;

        this.globalVariables = globalVariables;

        for (List<Quadruple> quadrupleList : code) {
            CodeSequence sequence = new CodeSequence();

            sequence.function = functions.get(i);
            sequence.labelIndices = BasicBlock.generateMapOfLabels(quadrupleList);
            sequence.basicBlocks = BasicBlock.extractBasicBlocksFromSequence(sequence.function, quadrupleList, sequence.labelIndices);

            assert sequence.basicBlocks != null;
            for (BasicBlock b : sequence.basicBlocks) {
                if (b.isEntryBlock()) {
                    sequence.entryBlock = b;
                    break;
                }
            }
            if (sequence.entryBlock == null)
                throw new RuntimeException("Invalid code sequence for loop discovery as entry block has not been found.");

            // update ENTER instruction and assign address to all variables
            Quadruple enterInstruction = sequence.entryBlock.instructions.get(1);

            Collection<Obj> allVariables = new HashSet<>();
            for (BasicBlock b : sequence.basicBlocks) {
                allVariables.addAll(b.extractAllVariables());
                allVariables.addAll(new HashSet<>(sequence.function.getLocalSymbols()));
            }

            int oldAllocationValue = ((QuadrupleIntegerConst) enterInstruction.getArg1()).getValue();

            //System.out.println("Variables for " + sequence.function.getName());
            int lastSize = giveAddressToTemps(allVariables, oldAllocationValue);

            enterInstruction.setArg1(new QuadrupleIntegerConst(SystemV_ABI.alignTo16(lastSize)));

            codeSequenceList.add(sequence);
            i++;
        }
    }

    public static int giveAddressToAll(Collection<Obj> variables, int startValue) {
        for (Obj obj : variables) {
            int lastTaken = startValue;
            if (SystemV_ABI.alignTo16(lastTaken) - lastTaken < SystemV_ABI.getX64VariableSize(obj.getType()))
                lastTaken = SystemV_ABI.alignTo16(lastTaken);
            int thisVarAddress = lastTaken + SystemV_ABI.getX64VariableSize(obj.getType());
            obj.setAdr(thisVarAddress);
            startValue = thisVarAddress;

        }

        return startValue;
    }

    public static int giveAddressToTemps(Collection<Obj> variables, int startValue) {
        for (Obj obj : variables) {
            if (((obj.tempVar || obj.getName().startsWith(Config.prefix_phi)) || (obj.parameter && !obj.stackParameter)) && obj.getKind() != Obj.Con) {
                int lastTaken = startValue;
                if (SystemV_ABI.alignTo16(lastTaken) - lastTaken < SystemV_ABI.getX64VariableSize(obj.getType()))
                    lastTaken = SystemV_ABI.alignTo16(lastTaken);
                int thisVarAddress = lastTaken + SystemV_ABI.getX64VariableSize(obj.getType());
                obj.setAdr(thisVarAddress);
                startValue = thisVarAddress;
            }
        }

        return startValue;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (CodeSequence codeSequence : codeSequenceList) {
            if (codeSequence.inlined)
                continue;

            stringBuilder.append("-----------------------------------------------------------------------------------------\n");

            for (BasicBlock basicBlock : Optimizer.reassembleBasicBlocks(codeSequence.basicBlocks))
                for (Quadruple q : basicBlock.instructions)
                    stringBuilder.append(q).append(System.lineSeparator());

            stringBuilder.append("-----------------------------------------------------------------------------------------\n");
        }

        return stringBuilder.toString();
    }

    protected void addOptimizationPass(OptimizerPass optimizerPass) {
        optimizationList.add(optimizerPass);
    }

    public void executeOptimizations() {
        assert globalVariables != null;

        // non-SSA optimizations
        for (CodeSequence sequence : codeSequenceList) {
            // DO NOT REMOVE THIS LINE
            optimizationList.clear();

            addOptimizationPass(new ValueNumbering(sequence));
            addOptimizationPass(new FunctionInlining(sequence, codeSequenceList));
            addOptimizationPass(new CFGCleaner(sequence));

            for (OptimizerPass pass : optimizationList) {
                pass.optimize();
                pass.finalizePass();
            }
        }


        // SSA optimizations
        for (CodeSequence sequence : codeSequenceList) {
            if (sequence.inlined)
                continue;

            sequence.dominanceAnalyzer = new DominanceAnalyzer(sequence);
            SSAConverter ssaConverter = new SSAConverter(sequence.dominanceAnalyzer);

            // DO NOT REMOVE THIS LINE
            optimizationList.clear();

            // before SSA conversion
            if (Config.dump_dot_files) {
                // TODO: change 'dot' files output path in production
                DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\pp2\\test\\debug\\normal_cfg_pre_ssa_" + sequence.function + ".dot",
                        "C:\\Users\\jugos000\\IdeaProjects\\pp2\\test\\debug\\normal_rcfg_pre_ssa_" + sequence.function + ".dot",
                        sequence.dominanceAnalyzer.getBasicBlocks());
                DominanceAnalyzer.dumpDominatorTree("C:\\Users\\jugos000\\IdeaProjects\\pp2\\test\\debug\\dominator_tree_" + sequence.function + ".dot",
                        sequence.dominanceAnalyzer.getImmediateDominators());
                DominanceAnalyzer.dumpDominatorTree("C:\\Users\\jugos000\\IdeaProjects\\pp2\\test\\debug\\reverse_dominator_tree_" + sequence.function + ".dot",
                        sequence.dominanceAnalyzer.getReverseImmediateDominators());
            }

            ssaConverter.doPhiPlacement();
            ssaConverter.renameVariables();

            if (Config.dump_dot_files) {
                DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\pp2\\test\\debug\\ssa_cfg_pre_opt_" + sequence.function + ".dot",
                        "C:\\Users\\jugos000\\IdeaProjects\\pp2\\test\\debug\\ssa_rcfg_pre_opt_" + sequence.function + ".dot",
                        sequence.dominanceAnalyzer.getBasicBlocks());
            }

            // TODO: uninitialized has to be done before inlining
            //addOptimizationPass(new UninitializedVariableDetection(sequence, globalVariables));
            //addOptimizationPass(new LoopInvariantCodeMotion(sequence));
            //addOptimizationPass(new DeadCodeElimination(sequence)); // always call CFGCleaner after DCE
            //addOptimizationPass(new CFGCleaner(sequence));
            for (OptimizerPass pass : optimizationList) {
                pass.optimize();
                pass.finalizePass();
            }

            if (Config.dump_dot_files) {
                DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\microjava_x64\\test\\debug\\ssa_cfg_post_opt_" + sequence.function + ".dot",
                        "C:\\Users\\jugos000\\IdeaProjects\\pp2\\test\\debug\\ssa_rcfg_post_opt_" + sequence.function + ".dot",
                        sequence.dominanceAnalyzer.getBasicBlocks());
            }

            // eliminating SSA from code
            ssaConverter.toNormalForm();

            if (Config.dump_dot_files) {
                DominanceAnalyzer.dumpCFG("C:\\Users\\jugos000\\IdeaProjects\\pp2\\test\\debug\\normal_cfg_post_ssa_" + sequence.function + ".dot",
                        "C:\\Users\\jugos000\\IdeaProjects\\pp2\\test\\debug\\normal_rcfg_post_ssa_" + sequence.function + ".dot",
                        sequence.dominanceAnalyzer.getBasicBlocks());
            }
        }

        // TODO: optimize frame size in ENTER instructions
    }

    public List<CodeSequence> getOptimizationOutput() {
        return codeSequenceList;
    }

    public static List<BasicBlock> reassembleBasicBlocks(List<BasicBlock> cfg) {
        /*List<BasicBlock> result = new ArrayList<>();


        Stack<BasicBlock> stack = new Stack<>();
        BasicBlock currentBlock = cfg.stream().filter(BasicBlock::isEntryBlock).collect(Collectors.toList()).get(0);
        // current block is the entry block

        int index = 0;
        while (index < cfg.size()) {
            if (cfg.size() == 1 || currentBlock.successor.size() == 0)
                result.add(currentBlock);
            else if (currentBlock.successor.size() == 1) {
                Quadruple lastInstruction = currentBlock.instructions.get(currentBlock.instructions.size() - 1);

                result.add(currentBlock);
                if (lastInstruction.getInstruction() == IRInstruction.JMP)
                    currentBlock = stack.pop();
                else
                    currentBlock = currentBlock.successor.get(0);
            } else if (currentBlock.successor.size() == 2) {
                Quadruple lastInstruction = currentBlock.instructions.get(currentBlock.instructions.size() - 1);

                QuadrupleLabel jumpDestination = (QuadrupleLabel) lastInstruction.getResult();

                BasicBlock successor1 = currentBlock.successor.get(0);
                BasicBlock successor2 = currentBlock.successor.get(1);

                BasicBlock addToStack = successor2;
                for (int i = 0; i < successor1.instructions.size(); i++) {
                    Quadruple q = successor1.instructions.get(i);
                    if (q.getInstruction() == IRInstruction.GEN_LABEL) {
                        if (((QuadrupleLabel) q.getArg1()).getLabelName().equals(jumpDestination.getLabelName())) {
                            addToStack = successor1;
                            break;
                        }
                    }
                }

                if (stack.isEmpty() || stack.peek() != addToStack)
                    stack.push(addToStack);
                result.add(currentBlock);
                currentBlock = (addToStack == successor1 ? successor2 : successor1);
            } else
                throw new RuntimeException("Basic block cannot have more than two successors.");

            // solution for IF without ELSE
            if (!stack.isEmpty() && stack.peek() == currentBlock)
                stack.pop();

            index++;
        }

        assert result.size() == result.stream().distinct().count();*/

        return cfg;
    }
}
