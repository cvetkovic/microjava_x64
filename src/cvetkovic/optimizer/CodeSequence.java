package cvetkovic.optimizer;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.ssa.DominanceAnalyzer;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.List;
import java.util.Map;

public class CodeSequence {
    public Obj function;

    public List<BasicBlock> basicBlocks;
    public Map<String, Integer> labelIndices;

    public BasicBlock entryBlock;
    public DominanceAnalyzer dominanceAnalyzer;
}
