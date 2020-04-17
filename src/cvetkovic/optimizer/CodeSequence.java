package cvetkovic.optimizer;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.List;
import java.util.Map;

public class CodeSequence {
    public Obj function;
    public List<Quadruple> code;
    public List<BasicBlock> basicBlocks;
    public Map<String, Integer> labelIndices;
    //public List<Set<BasicBlock>> loops;

    public BasicBlock entryBlock;
}
