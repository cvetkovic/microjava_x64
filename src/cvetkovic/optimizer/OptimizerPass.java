package cvetkovic.optimizer;

public interface OptimizerPass {
    void optimize();
    void finalizePass();
}
