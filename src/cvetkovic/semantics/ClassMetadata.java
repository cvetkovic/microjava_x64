package cvetkovic.semantics;

import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashMap;
import java.util.Map;

public class ClassMetadata {
    public Obj classObj;
    public Map<String, Obj> pointersToFunction = new HashMap<>();
}
