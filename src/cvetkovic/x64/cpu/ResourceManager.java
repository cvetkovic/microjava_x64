package cvetkovic.x64.cpu;

import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;

public class ResourceManager {
    private Map<Obj, PriorityQueue<Descriptor>> referencesToDescriptors = new HashMap<>();
    private static DescriptorComparator comparator = new DescriptorComparator();

    private List<RegisterDescriptor> unoccupiedRegisters;

    public ResourceManager(List<RegisterDescriptor> unoccupiedRegisters) {
        this.unoccupiedRegisters = unoccupiedRegisters;
    }

    public void validate(Obj obj, Descriptor descriptor) {
        if (referencesToDescriptors.containsKey(obj))
            referencesToDescriptors.get(obj).add(descriptor);
        else {
            PriorityQueue<Descriptor> queue = new PriorityQueue<>(comparator);
            queue.add(descriptor);
            referencesToDescriptors.put(obj, queue);
        }
    }

    public Descriptor getLocation(Obj obj) {
        if (referencesToDescriptors.containsKey(obj))
            return referencesToDescriptors.get(obj).peek();
        else
            return null;
    }

    public void invalidate(Obj obj) {
        Queue<Descriptor> queue = referencesToDescriptors.get(obj);

        if (queue != null)
            queue.forEach(p -> p.holdsValueOf = null);
        else
            referencesToDescriptors.put(obj, new PriorityQueue<>(comparator));
    }

    private static class DescriptorComparator implements Comparator<Descriptor> {
        @Override
        public int compare(Descriptor o1, Descriptor o2) {
            if (o1 instanceof RegisterDescriptor && o2 instanceof AddressDescriptor)
                return -1;
            else if (o1 instanceof AddressDescriptor && o2 instanceof RegisterDescriptor)
                return 1;
            else
                return 0;
        }
    }

    public RegisterDescriptor getRegister(Obj obj, List<String> out) {
        if (referencesToDescriptors.containsKey(obj)) {
            PriorityQueue<Descriptor> queue = referencesToDescriptors.get(obj);

            if (queue.size() > 0 && queue.peek() instanceof RegisterDescriptor)
                return (RegisterDescriptor)queue.peek();                // in register
            else if (queue.size() > 0 && !(queue.peek() instanceof RegisterDescriptor) && unoccupiedRegisters.size() > 0)
                return unoccupiedRegisters.get(0);  // not in register, but there is a free register
            else
                return getRegisterComplex(obj);
        }
        else {
            if (unoccupiedRegisters.size() > 0)
                return unoccupiedRegisters.get(0);  // not in register, but there is a free register
            else
                return getRegisterComplex(obj);
        }
    }

    private RegisterDescriptor getRegisterComplex(Obj obj) {
        throw new RuntimeException("Not implemented yet.");
    }
}
