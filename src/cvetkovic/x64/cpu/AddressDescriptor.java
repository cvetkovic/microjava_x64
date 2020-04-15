package cvetkovic.x64.cpu;

/**
 * Address descriptor represents where the obj node is currently located. It could be either in memory or in both
 * memory and register. Variable can be in at most one register. Address descriptors should be created for all
 * the variables, both non-temporary and temporary ones.
 */
public class AddressDescriptor {
    private MemoryDescriptor memoryLocation;
    private RegisterDescriptor registerLocation;

    public AddressDescriptor(MemoryDescriptor memoryLocation) {
        this.memoryLocation = memoryLocation;
    }

    public void setRegisterLocation(RegisterDescriptor registerLocation) {
        this.registerLocation = registerLocation;
    }

    public Descriptor getDescriptor() {
        return (registerLocation == null ? memoryLocation : registerLocation);
    }

    public MemoryDescriptor getMemoryDescriptor() {
        return memoryLocation;
    }
}
