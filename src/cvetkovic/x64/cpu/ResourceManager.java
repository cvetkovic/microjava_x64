package cvetkovic.x64.cpu;

import cvetkovic.ir.optimizations.BasicBlock;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.QuadrupleObjVar;
import cvetkovic.x64.SystemV_ABI;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceManager {
    private static DescriptorComparator staticQueueComparator = new DescriptorComparator();

    private Map<Obj, PriorityQueue<Descriptor>> addressDescriptors = new HashMap<>();
    private Map<Obj, MemoryDescriptor> memoryDescriptors = new HashMap<>();

    private List<RegisterDescriptor> freeRegisters;
    private List<RegisterDescriptor> allRegisters = new ArrayList<>();
    private Set<Obj> dirtyVariables = new HashSet<>();

    private int sizeOfTempVars = 0;

    public ResourceManager(List<RegisterDescriptor> freeRegisters, List<BasicBlock.Tuple<Obj, Boolean>> variables) {
        this.freeRegisters = freeRegisters;
        this.allRegisters.addAll(freeRegisters);

        createAddressDescriptors(variables);
        sizeOfTempVars = calculateSizeOfTempVariables(variables);
    }

    private void createAddressDescriptors(List<BasicBlock.Tuple<Obj, Boolean>> variables) {
        for (BasicBlock.Tuple<Obj, Boolean> tuple : variables) {
            if (tuple.u.getKind() == Obj.Con)
                continue;

            MemoryDescriptor descriptor = new MemoryDescriptor(tuple.u, tuple.v);
            PriorityQueue<Descriptor> queue = new PriorityQueue<>(staticQueueComparator);
            queue.add(descriptor);

            addressDescriptors.put(tuple.u, queue);
            memoryDescriptors.put(tuple.u, descriptor);
        }
    }

    private int calculateSizeOfTempVariables(List<BasicBlock.Tuple<Obj, Boolean>> variables) {
        int tempVarSize = 0;

        List<Obj> tempVars = variables.stream().filter(p -> p.u.tempVar).map(p -> p.u).collect(Collectors.toList());
        for (Obj obj : tempVars)
            tempVarSize += SystemV_ABI.getX64VariableSize(obj.getType());

        return tempVarSize;
    }

    public int getSizeOfTempVars() {
        return sizeOfTempVars;
    }

    /**
     * Returns memory descriptor of the provided obj var
     *
     * @param var
     * @return
     */
    public MemoryDescriptor getAddressDescriptor(Obj var) {
        return memoryDescriptors.get(var);
    }

    /**
     * Invalidates what's currently in this descriptor and validates it new obj node
     *
     * @param newObj           Object node that will be assigned to target descriptor
     * @param targetDescriptor Target descriptor that will be taken with obj
     */
    public void validate(Descriptor targetDescriptor, Obj newObj, List<String> aux, boolean setDirty) {
        PriorityQueue<Descriptor> newObjQueue = addressDescriptors.get(newObj);
        if (newObjQueue == null) {
            newObjQueue = new PriorityQueue<>(staticQueueComparator);
            addressDescriptors.put(newObj, newObjQueue);
        }
        targetDescriptor.holdsValueOf = newObj;
        if (!newObjQueue.contains(targetDescriptor))
            newObjQueue.add(targetDescriptor);

        if (setDirty)
            dirtyVariables.add(newObj);
    }

    /**
     * Load operand to specified register if it's not already there
     *
     * @param register
     * @param operand
     * @param out
     */
    public void fetchOperand(RegisterDescriptor register, Obj operand, List<String> out) {
        if (register != null && freeRegisters.contains(register))
            freeRegisters.remove(register);

        if (operand.getKind() == Obj.Con) {
            register.setPrintWidth(SystemV_ABI.getX64VariableSize(operand.getType()));
            out.add("\tMOV " + register + ", " + operand.getAdr());
        }
        else if (register.holdsValueOf == operand)
            return;
        else if (register.holdsValueOf != operand) {
            PriorityQueue<Descriptor> newObjQueue = addressDescriptors.get(operand);
            if (newObjQueue == null) {
                newObjQueue = new PriorityQueue<>(staticQueueComparator);
                addressDescriptors.put(operand, newObjQueue);
            }

            Descriptor operandDescriptor = newObjQueue.peek();

            register.holdsValueOf = operand;
            register.setPrintWidth(SystemV_ABI.getX64VariableSize(operandDescriptor.getHoldsValueOf().getType()));
            out.add("\tMOV " + register + ", " + operandDescriptor);

            newObjQueue.add(register);
        }
    }

    public void invalidate(Descriptor targetDescriptor, Obj newObj, List<String> aux) {
        if (targetDescriptor instanceof MemoryDescriptor)
            return;

        Obj oldObj = targetDescriptor.holdsValueOf;
        targetDescriptor.holdsValueOf = null;

        if (oldObj == null || oldObj == newObj)
            return;
        else {
            PriorityQueue<Descriptor> oldObjQueue = addressDescriptors.get(oldObj);
            // count the number of other registers that hold oldObj
            int numberOfRegister = (int) oldObjQueue.stream().filter(p -> p instanceof RegisterDescriptor && p != targetDescriptor).count();

            // don't save old obj unless it's dirty
            if (!dirtyVariables.contains(oldObj))
                return;

            // if other registers hold the same value do nothing
            if (numberOfRegister > 0)
                return;
            else {
                // must save the old obj
                ((RegisterDescriptor) targetDescriptor).setPrintWidth(SystemV_ABI.getX64VariableSize(oldObj.getType()));

                aux.add("\tMOV " + memoryDescriptors.get(oldObj) + ", " + targetDescriptor);
                dirtyVariables.remove(oldObj);
            }

            // remove old obj from its queue
            oldObjQueue.remove(targetDescriptor);
        }
    }

    private static class DescriptorComparator implements Comparator<Descriptor> {
        @Override
        public int compare(Descriptor o1, Descriptor o2) {
            if (o1 instanceof RegisterDescriptor && o2 instanceof MemoryDescriptor)
                return -1;
            else if (o1 instanceof MemoryDescriptor && o2 instanceof RegisterDescriptor)
                return 1;
            else
                return 0;
        }
    }

    public RegisterDescriptor getRegister(Obj obj, Quadruple instruction) {
        PriorityQueue<Descriptor> queue = addressDescriptors.get(obj);
        MemoryDescriptor addressDescriptor = memoryDescriptors.get(obj);

        if (queue != null && queue.peek() instanceof RegisterDescriptor)
            // CASE: obj is already in register, no action needed
            return (RegisterDescriptor) queue.peek();
        else if ((queue == null || queue.peek() instanceof MemoryDescriptor) && freeRegisters.size() > 0) {
            // CASE: var not encountered before or is not in a register and there are free registers
            // take a register from the list and return
            RegisterDescriptor register = freeRegisters.get(0);

            // TODO: if queue is null then then ISSUE MOV out.add("\tmov " + register + ", " + (queue != null ? addressDescriptor : obj) + "");

            return register;
        }
        else {
            RegisterDescriptor duplicate = getRegisterIfDuplicated();
            if (duplicate != null)
                return duplicate;

            RegisterDescriptor twoAppearancesInSingleInstruction = getRegisterIfDestination(instruction);
            if (twoAppearancesInSingleInstruction != null)
                return twoAppearancesInSingleInstruction;

            RegisterDescriptor livenessRegister = getRegisterByLiveness(instruction);
            if (livenessRegister != null)
                return livenessRegister;

            // return any register
            return allRegisters.get(0);
        }
    }

    /**
     * If v is not used later (that is, after the instruction I, there
     * are no further uses of v, and if v is live on exit from the block, then
     * v is recomputed within the block).
     *
     * @return Register descriptor if condition is satisfied, otherwise null.
     */
    private RegisterDescriptor getRegisterByLiveness(Quadruple instruction) {
        Obj obj1 = ((QuadrupleObjVar) instruction.getArg1()).getObj();
        Obj obj2 = (instruction.getArg2() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) instruction.getArg2()).getObj() : null);
        Obj objResult = ((QuadrupleObjVar) instruction.getResult()).getObj();


        if (obj1 != null && instruction.getArg1NextUse() == Quadruple.NextUseState.DEAD) {
            PriorityQueue<Descriptor> arg1Queue = addressDescriptors.get(obj1);
            if (arg1Queue != null && arg1Queue.peek() instanceof RegisterDescriptor)
                return (RegisterDescriptor) arg1Queue.peek();
        }
        else if (obj2 != null && instruction.getArg2NextUse() == Quadruple.NextUseState.DEAD) {
            PriorityQueue<Descriptor> arg2Queue = addressDescriptors.get(obj2);
            if (arg2Queue != null && arg2Queue.peek() instanceof RegisterDescriptor)
                return (RegisterDescriptor) arg2Queue.peek();
        }
        else if (objResult != null && instruction.getResultNextUse() == Quadruple.NextUseState.DEAD) {
            PriorityQueue<Descriptor> resultQueue = addressDescriptors.get(objResult);
            if (resultQueue != null && resultQueue.peek() instanceof RegisterDescriptor)
                return (RegisterDescriptor) resultQueue.peek();
        }

        return null;
    }

    /**
     * If v is x, the variable being computed by instruction I, and x is not
     * also one of the other operands of instruction I (z in this example),
     * then we are OK. The reason is that in this case, we know this value
     * of x is never again going to be used, so we are free to ignore it.
     *
     * @param instruction Quadruple instruction.
     * @return Register descriptor if condition is satisfied, otherwise null.
     */
    private RegisterDescriptor getRegisterIfDestination(Quadruple instruction) {
        if ((!(instruction.getArg1() instanceof QuadrupleObjVar) && !(instruction.getArg2() instanceof QuadrupleObjVar) && !(instruction.getResult() instanceof QuadrupleObjVar)) ||
                (!(instruction.getArg1() instanceof QuadrupleObjVar) && !(instruction.getArg2() == null) && !(instruction.getResult() instanceof QuadrupleObjVar)))
            return null;

        Obj obj1 = ((QuadrupleObjVar) instruction.getArg1()).getObj();
        Obj obj2 = (instruction.getArg2() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) instruction.getArg2()).getObj() : null);
        Obj objResult = ((QuadrupleObjVar) instruction.getResult()).getObj();

        for (RegisterDescriptor reg : allRegisters) {
            if (reg.holdsValueOf == objResult) {
                if (reg.holdsValueOf != obj1 && (obj2 == null || obj2 != reg.holdsValueOf))
                    return reg;
            }
        }

        return null;
    }

    /**
     * Instruction is x = y + z. Prospective register is R that holds v obj node.
     * If multiple registers hold v, than return R.
     *
     * @return Register descriptor if condition is satisfied, otherwise null.
     */
    private RegisterDescriptor getRegisterIfDuplicated() {
        SortedMap<Integer, RegisterDescriptor> duplicates = new TreeMap<>(Comparator.reverseOrder());

        for (RegisterDescriptor descriptor : allRegisters) {
            int num = 0;

            if (descriptor.holdsValueOf != null)
                num++;  // add myself
            else
                continue;

            // add all the other register descriptors but myself who hold the same obj as me
            num += addressDescriptors.get(descriptor.holdsValueOf).stream().filter(p -> p != descriptor && p instanceof RegisterDescriptor).count();

            // NOTE: if we have same number of appearances only one entry would be present in SortedMap
            duplicates.put(num, descriptor);
        }

        // TODO: remove register references in referenceToMemory map

        if (duplicates.firstKey() > 1)
            return duplicates.get(duplicates.firstKey());
        else
            return null;
    }

    /**
     * This method is invoked on end of basic block's code generation
     * in order to save all non-temporary variables to memory that
     * were made dirty during the execution
     */
    public void saveDirtyVariables(List<String> out, boolean saveTemps) {
        for (Obj obj : dirtyVariables) {
            if (!saveTemps && obj.tempVar)
                continue;

            PriorityQueue<Descriptor> queue = addressDescriptors.get(obj);

            // the only case where we need to save is when RegisterDescriptor is on top of the queue
            if (queue.size() > 0 && queue.peek() instanceof RegisterDescriptor)
                out.add("\tMOV " + memoryDescriptors.get(obj) + ", " + queue.peek().toString());
        }

        dirtyVariables.clear();
    }

    public RegisterDescriptor getRegisterByForce(List<String> out) {
        // TODO: change allocation algorithm do this with LRU
        RegisterDescriptor register;

        if (freeRegisters.size() > 0) {
            register = freeRegisters.get(0);
            freeRegisters.remove(register);
        }
        else {
            for (PriorityQueue<Descriptor> queue : addressDescriptors.values()) {
                if (queue.peek() instanceof RegisterDescriptor) {
                    register = (RegisterDescriptor) queue.poll();
                    out.add("\tmov " + addressDescriptors.get(register.holdsValueOf) + ", " + register);
                    register.holdsValueOf = null;

                    break;
                }
            }

            throw new RuntimeException("Register not allocatable by force.");
        }

        return register;
    }

    /**
     * Checks whether object node is loaded in memory
     *
     * @param obj
     * @return
     */
    public boolean checkIfObjIsInRegister(Obj obj) {
        return addressDescriptors.get(obj).peek() instanceof RegisterDescriptor;
    }

    /**
     * Checks whether register is free
     *
     * @param descriptor
     * @return
     */
    public boolean checkIfRegisterIsTaken(RegisterDescriptor descriptor) {
        return !freeRegisters.contains(descriptor);
    }

    //////////////////////////////////////// CONTEXT PRESERVATION

    /*
    Only RBP, RBX, R12-R15 should be saved by callee -> ABI 3.2.1
     */

    public void saveContext(List<RegisterDescriptor> usedRegisters, List<String> out) {
        for (int i = 0; i < usedRegisters.size(); i++) {
            usedRegisters.get(i).setPrintWidth(8);
            out.add("\tPUSH " + usedRegisters.get(i));
        }
    }

    public void restoreContext(List<RegisterDescriptor> usedRegisters, List<String> out) {
        for (int i = 0; i < usedRegisters.size(); i++) {
            usedRegisters.get(i).setPrintWidth(8);
            out.add("\tPOP " + usedRegisters.get(i));
        }
    }
}
