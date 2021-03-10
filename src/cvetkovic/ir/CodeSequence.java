package cvetkovic.ir;

import cvetkovic.algorithms.DominanceAnalyzer;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.List;
import java.util.Map;

public class CodeSequence {
    public Obj function;

    public List<BasicBlock> basicBlocks;
    public Map<String, Integer> labelIndices;

    public BasicBlock entryBlock;
    public DominanceAnalyzer dominanceAnalyzer;

    public boolean inlined = false;
}
