package cvetkovic.x64.cpu;

import cvetkovic.ir.optimizations.BasicBlock;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;

public class ResourceManager {
    private Map<Obj, PriorityQueue<Descriptor>> referencesToMemory = new HashMap<>();
    private Map<Obj, AddressDescriptor> referencesToAddressDescriptors = new HashMap<>();

    private static DescriptorComparator queueComparator = new DescriptorComparator();

    private List<RegisterDescriptor> unoccupiedRegisters;
    private Set<Obj> dirtyVariables = new HashSet<>();

    public ResourceManager(List<RegisterDescriptor> unoccupiedRegisters, List<BasicBlock.Tuple<Obj, Boolean>> variables) {
        this.unoccupiedRegisters = unoccupiedRegisters;

        createAddressRegisters(variables);
    }

    private void createAddressRegisters(List<BasicBlock.Tuple<Obj, Boolean>> variables) {
        for (BasicBlock.Tuple<Obj, Boolean> tuple : variables) {
            AddressDescriptor descriptor = new AddressDescriptor(tuple.u, tuple.v);
            PriorityQueue<Descriptor> queue = new PriorityQueue<>(queueComparator);
            queue.add(descriptor);

            referencesToMemory.put(tuple.u, queue);
            referencesToAddressDescriptors.put(tuple.u, descriptor);
        }
    }

    private void allocateSpaceForTemporaryVariables() {
        throw new RuntimeException("Not yet implemented.");
    }

    /**
     * Invalidates what's currently in this descriptor and validates it new obj node
     *
     * @param newObj           Object node that will be assigned to target descriptor
     * @param targetDescriptor Target descriptor that will be taken with obj
     */
    public void validate(Obj newObj, Descriptor targetDescriptor) {
        // deleting old reference
        Obj oldObj = targetDescriptor.holdsValueOf;
        PriorityQueue<Descriptor> oldObjQueue = referencesToMemory.get(oldObj);
        if (oldObjQueue != null)
            oldObjQueue.remove(targetDescriptor);

        PriorityQueue<Descriptor> newObjQueue = referencesToMemory.get(newObj);
        if (newObjQueue == null) {
            newObjQueue = new PriorityQueue<>(queueComparator);
            referencesToMemory.put(newObj, newObjQueue);
        }
        newObjQueue.add(targetDescriptor);
    }

    public void invalidate(Obj obj) {
        Queue<Descriptor> queue = referencesToMemory.get(obj);

        if (queue != null)
            queue.forEach(p -> p.holdsValueOf = null);
        else
            referencesToMemory.put(obj, new PriorityQueue<>(queueComparator));
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
        return getRegister(obj, out, false);
    }

    public RegisterDescriptor getRegister(Obj obj, List<String> out, boolean forseMemory) {
        PriorityQueue<Descriptor> queue = referencesToMemory.get(obj);
        AddressDescriptor addressDescriptor = referencesToAddressDescriptors.get(obj);

        if (queue != null && queue.peek() instanceof RegisterDescriptor)
            return (RegisterDescriptor) queue.peek();
        else if ((queue == null || queue.peek() instanceof AddressDescriptor) && unoccupiedRegisters.size() > 0) {
            RegisterDescriptor register = unoccupiedRegisters.get(0);
            unoccupiedRegisters.remove(0);

            out.add("\tmov " + register + ", " + addressDescriptor + "");

            return register;
        }
        else {
            return null;
        }
    }

    private RegisterDescriptor getRegisterComplex(Obj obj) {
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * This method is invoked on end of basic block's code generation
     * in order to save all non-temporary variables to memory that
     * were made dirty during the execution
     */
    public void saveDirtyVariables(List<String> out) {
        for (Obj obj : dirtyVariables) {
            PriorityQueue<Descriptor> queue = referencesToMemory.get(obj);

            // the only case where we need to save is when RegisterDescriptor is on top of the queue
            if (queue.size() > 0 && queue.peek() instanceof RegisterDescriptor)
                out.add("mov " + referencesToAddressDescriptors.get(obj) + ", " + queue.peek().toString() + System.lineSeparator());
        }
    }

    public void saveContext() {

    }

    public void restoreContext() {

    }
}
