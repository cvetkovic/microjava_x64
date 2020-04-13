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

        createAddressDescriptors(variables);
    }

    private void createAddressDescriptors(List<BasicBlock.Tuple<Obj, Boolean>> variables) {
        for (BasicBlock.Tuple<Obj, Boolean> tuple : variables) {
            AddressDescriptor descriptor = new AddressDescriptor(tuple.u, tuple.v);
            PriorityQueue<Descriptor> queue = new PriorityQueue<>(queueComparator);
            queue.add(descriptor);

            referencesToMemory.put(tuple.u, queue);
            referencesToAddressDescriptors.put(tuple.u, descriptor);
        }
    }

    public AddressDescriptor getAddressDescriptor(Obj var) {
        return referencesToAddressDescriptors.get(var);
    }

    /**
     * Invalidates what's currently in this descriptor and validates it new obj node
     *
     * @param newObj           Object node that will be assigned to target descriptor
     * @param targetDescriptor Target descriptor that will be taken with obj
     */
    public void validate(Obj newObj, Descriptor targetDescriptor, boolean setDirty) {
        PriorityQueue<Descriptor> newObjQueue = referencesToMemory.get(newObj);
        if (newObjQueue == null) {
            newObjQueue = new PriorityQueue<>(queueComparator);
            referencesToMemory.put(newObj, newObjQueue);
        }
        targetDescriptor.holdsValueOf = newObj;
        newObjQueue.add(targetDescriptor);

        if (setDirty && !newObj.tempVar)
            dirtyVariables.add(newObj);
    }

    public void invalidateFromRegister(Descriptor targetDescriptor, List<String> aux) {
        Obj oldObj = targetDescriptor.holdsValueOf;
        PriorityQueue<Descriptor> oldObjQueue = referencesToMemory.get(oldObj);
        if (oldObjQueue != null && oldObjQueue.peek() instanceof RegisterDescriptor) {
            // if target descriptor has dirty variable save it to memory
            if (dirtyVariables.contains(targetDescriptor.holdsValueOf)) {
                aux.add("\tmov " + referencesToAddressDescriptors.get(targetDescriptor.holdsValueOf) + ", " + targetDescriptor);
                dirtyVariables.remove(targetDescriptor.holdsValueOf);
            }

            oldObjQueue.remove(targetDescriptor);
        }
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

    public Descriptor getRegister(Obj obj, List<String> out) {
        return getRegister(obj, out, false);
    }

    public Descriptor getRegister(Obj obj, List<String> out, boolean forceMemory) {
        PriorityQueue<Descriptor> queue = referencesToMemory.get(obj);
        AddressDescriptor addressDescriptor = referencesToAddressDescriptors.get(obj);

        if (queue != null && queue.peek() instanceof RegisterDescriptor)
            return queue.peek();
        else if (forceMemory)
            return addressDescriptor;
        else if ((queue == null || queue.peek() instanceof AddressDescriptor) && unoccupiedRegisters.size() > 0) {
            RegisterDescriptor register = unoccupiedRegisters.get(0);
            unoccupiedRegisters.remove(0);

            register.holdsValueOf = obj;
            out.add("\tmov " + register + ", " + (queue != null ? addressDescriptor : obj) + "");

            return register;
        }
        else {
            return null;
        }
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
                out.add("\tmov " + referencesToAddressDescriptors.get(obj) + ", " + queue.peek().toString());
        }

        dirtyVariables.clear();
    }

    public void saveContext(List<RegisterDescriptor> usedRegisters) {
        /*
        Only RBP, RBX, R12-R15 should be saved by callee -> ABI 3.2.1
         */
    }

    public void restoreContext(List<RegisterDescriptor> usedRegisters) {

    }
}
