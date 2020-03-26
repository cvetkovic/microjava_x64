package cvetkovic.ir;

import cvetkovic.ir.quadruple.Quadruple;

public class ControlFlow {
    private static int uniqueID = 0;

    public static String generateUniqueLabelName() {
        return "L" + uniqueID++;
    }

    public static class IfFixPoint {
        public Quadruple fixPoint;
        public int depth;
        public FixType fixType;

        public IfFixPoint(Quadruple fixPoint, int depth) {
            this.fixPoint = fixPoint;
            this.depth = depth;
            this.fixType = FixType.CODE_INSERTED;
        }

        public IfFixPoint(Quadruple fixPoint, int depth, FixType fixType) {
            this.fixPoint = fixPoint;
            this.depth = depth;
            this.fixType = fixType;
        }

        public enum FixType {
            CODE_INSERTED,
            COMPILER_ADDED
        }
    }
}
