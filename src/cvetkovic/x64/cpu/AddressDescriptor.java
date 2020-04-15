package cvetkovic.x64.cpu;

public class AddressDescriptor {
    public MemoryDescriptor memoryLocation;
    public RegisterDescriptor registerLocation;

    public AddressDescriptor(MemoryDescriptor memoryLocation) {
        this.memoryLocation = memoryLocation;
    }
}
