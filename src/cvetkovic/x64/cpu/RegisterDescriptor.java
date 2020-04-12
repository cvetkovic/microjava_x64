package cvetkovic.x64.cpu;

public class RegisterDescriptor extends Descriptor {
    protected String ISAName;

    public RegisterDescriptor(String ISAName) {
        this.ISAName = ISAName;
    }

    @Override
    public String toString() {
        return ISAName;
    }
}
