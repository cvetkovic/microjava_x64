package cvetkovic.misc;

import cvetkovic.ir.optimizations.BasicBlock;

import java.util.List;
import java.util.Set;

public class Utility {
    public static String printCycle(List<Set<BasicBlock>> loops) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Set<BasicBlock> loop : loops) {
            StringBuilder s = new StringBuilder();
            s.append("(");
            loop.forEach(x -> s.append(x.blockId + ", "));
            s.append(")");

            stringBuilder.append(s.toString().replace(", )", ")"));
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
