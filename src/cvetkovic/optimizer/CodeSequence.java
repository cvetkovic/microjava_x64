package cvetkovic.optimizer;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;

import java.util.List;
import java.util.Map;

public class CodeSequence {
    public List<Quadruple> code;
    public List<BasicBlock> basicBlocks;
    public Map<String, Integer> labelIndices;
    //public List<Set<BasicBlock>> loops;

    public BasicBlock entryBlock;
}
