package cvetkovic.optimizer.passes;

public interface OptimizerPass {
    void optimize();
    void finalizePass();
}
