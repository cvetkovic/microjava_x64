package cvetkovic.misc;

public class Config {
    public static boolean printBasicBlockInfo = false;

    public static boolean printDominatorRelations = true;
    public static boolean printNaturalLoopsInfo = true;

    public static final String prefix_phi = "_phi";
    public static final String leftoversBlockPrefix = "_INLINED";

    public static int inlinedCounter = 0;
    public static int leftoversLabelGenerator = 0;

    public static boolean dump_dot_files = true;
}
