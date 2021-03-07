package cvetkovic.misc;

public class Config {
    public static boolean printBasicBlockInfo = false;
    public static boolean printBasicBlockQuadruples = false;
    public static boolean printBasicBlockGlobalLivenessAnalysisTable = false;

    public static boolean printIRCodeLivenessAnalysis = false;

    public static final String prefix_phi = "_phi";
    public static final String compare_tmp = "_ctmp";
    public static final String leftoversBlockPrefix = "_inlined";

    public static int inlinedAddressOffset = 0;
    public static int inlinedCounter = 0;
    public static int leftoversLabelGenerator = 0;
}
