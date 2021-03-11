package cvetkovic.misc;

public class Config {
    public static boolean printBasicBlockInfo = false;

    public static boolean printDominatorRelations = false;
    public static boolean printNaturalLoopsInfo = false;

    public static final String prefix_phi = "_phi";
    public static final String leftoversBlockPrefix = "_INLINED";

    public static int inlinedCounter = 0;
    public static int leftoversLabelGenerator = 0;
    public static final String inlinedVarClonesPrefix = "_inl_";
}
