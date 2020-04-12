package cvetkovic.semantics;

import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashMap;
import java.util.Map;

public class ClassMetadata {
    public String className;
    public Map<String, Obj> pointersToFunction = new HashMap<>();
}
