package cvetkovic.x64;

import cvetkovic.misc.Tuple;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.x64.cpu.AddressDescriptor;
import cvetkovic.x64.cpu.Descriptor;
import cvetkovic.x64.cpu.MemoryDescriptor;
import cvetkovic.x64.cpu.RegisterDescriptor;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class ResourceManager {
    private Map<Obj, AddressDescriptor> addressDescriptors = new HashMap<>();
    private List<RegisterDescriptor> allRegisters = new ArrayList<>();

    private Set<Obj> dirtyVariables = new HashSet<>();
    private List<RegisterDescriptor> freeRegisters;

    private int sizeOfTempVars = 0;

    private Map<String, RegisterDescriptor> _64_bit_name_to_reg_descriptor = new HashMap<>();

    public ResourceManager(List<RegisterDescriptor> freeRegisters) {
        this.freeRegisters = freeRegisters;
        this.allRegisters.addAll(freeRegisters);

        this.allRegisters.forEach(p -> _64_bit_name_to_reg_descriptor.put(p.ISA_8_ByteName, p));
    }

    public void configureAddressDescriptors(List<Tuple<Obj, Boolean>> variables) {
        createAddressDescriptors(variables);
    }

    /**
     * Creates address descriptor instances for both non-temporary and temporary variables
     *
     * @param variables
     */
    private void createAddressDescriptors(List<Tuple<Obj, Boolean>> variables) {
        for (Tuple<Obj, Boolean> tuple : variables) {
            if (tuple.u.getKind() == Obj.Con)
                continue;

            AddressDescriptor addressDescriptor = new AddressDescriptor(new MemoryDescriptor(tuple.u, tuple.v));
            addressDescriptors.put(tuple.u, addressDescriptor);
        }
    }

    /**
     * Returns memory descriptor of the provided obj var
     *
     * @param var
     * @return
     */
    public MemoryDescriptor getMemoryDescriptor(Obj var) {
        return addressDescriptors.get(var).getMemoryDescriptor();
    }

    /**
     * Invalidates what's currently in this descriptor and validates it new obj node
     *
     * @param newObj           Object node that will be assigned to target descriptor
     * @param targetDescriptor Target descriptor that will be taken with obj
     */
    public void validate(Descriptor targetDescriptor, Obj newObj, List<String> aux, boolean setDirty) {
        Obj oldObj = targetDescriptor.holdsValueOf;

        if (oldObj != null) {
            AddressDescriptor descriptor = addressDescriptors.get(oldObj);
            descriptor.setRegisterLocation(null);

            if (newObj != oldObj && dirtyVariables.contains(oldObj)) {
                aux.add("\tMOV " + addressDescriptors.get(oldObj).getMemoryDescriptor() + ", " + targetDescriptor);
                dirtyVariables.remove(oldObj);
            }
        }

        if (newObj != null) {
            // setting new value
            AddressDescriptor addressDescriptor = addressDescriptors.get(newObj);
            targetDescriptor.holdsValueOf = newObj;
            addressDescriptor.setRegisterLocation((RegisterDescriptor) targetDescriptor);

            if (setDirty)
                dirtyVariables.add(newObj);
        }
    }

    /**
     * Saves the content of register into memory if register holds value, otherwise does nothing
     *
     * @param register
     * @param out
     */
    public void forceTransferToMemory(RegisterDescriptor register, List<String> out) {
        Obj oldObj = register.holdsValueOf;
        AddressDescriptor oldObjDescriptor = addressDescriptors.get(oldObj);
        if (oldObj != null && oldObjDescriptor.getDescriptor() instanceof RegisterDescriptor) {
            out.add("\tMOV " + oldObjDescriptor.getMemoryDescriptor() + ", " + oldObjDescriptor.getDescriptor());
            dirtyVariables.remove(oldObj);

            assert oldObjDescriptor.getDescriptor() == register;
            oldObjDescriptor.setRegisterLocation(null);
        }
    }

    public void saveReturnedValueToMemory(RegisterDescriptor reg, Obj destination, List<String> out) {
        if (addressDescriptors.get(destination) != null) {
            addressDescriptors.get(destination).setRegisterLocation(null);
            out.add("\tMOV " + addressDescriptors.get(destination).getMemoryDescriptor() + ", " + reg.getNameBySize(SystemV_ABI.getX64VariableSize(destination.getType())));
            dirtyVariables.remove(destination);
        }
    }

    private boolean sxd = false;

    public void setSXD() {
        sxd = true;
    }

    /**
     * Saves old value in register if dirty and load new value from memory. Issue those instruction and
     * remove from free register list if is present there.
     */
    public void fetchOperand(RegisterDescriptor register, Obj newObj, List<String> out) {
        if (newObj.getAdr() == -1)
            throw new RuntimeException("Address of obj cannot be negative. Operand fetching has failed.");

        try {
            if (register.holdsValueOf == newObj)
                return;
            else {
                // removing old variable
                Obj oldObj = register.holdsValueOf;
                AddressDescriptor oldObjDescriptor = addressDescriptors.get(oldObj);
                if (oldObj != null && oldObjDescriptor.getDescriptor() instanceof RegisterDescriptor) {
                    out.add("\tMOV " + oldObjDescriptor.getMemoryDescriptor() + ", " + oldObjDescriptor.getDescriptor());
                    dirtyVariables.remove(oldObj);

                    //assert oldObjDescriptor.getDescriptor() == register;
                    register.setHoldsValueOf(null);
                    oldObjDescriptor.setRegisterLocation(null);
                }

                if (newObj.getKind() == Obj.Con) {
                    int size = SystemV_ABI.getX64VariableSize(newObj.getType());
                    if (sxd)
                        size = 8;

                    out.add("\tMOV " + register.getNameBySize(size) + ", " + newObj.getAdr());
                    return;
                }

                // loading new variable
                AddressDescriptor newObjDescriptor = addressDescriptors.get(newObj);
                register.holdsValueOf = newObj;
                out.add("\t" + (sxd ? "MOVSXD " + register.getNameBySize(8) : "MOV " + register.getNameBySize(SystemV_ABI.getX64VariableSize(newObj.getType()))) + ", " + addressDescriptors.get(newObj).getDescriptor());

                newObjDescriptor.setRegisterLocation(register);
            }
        } finally {
            sxd = false;
        }
    }

    public void clearRegisterFromAddressDescriptors(Obj obj) {
        addressDescriptors.get(obj).setRegisterLocation(null);
    }

    public void pushParameter(RegisterDescriptor register, Obj newObj, List<String> out) {
        if (register.holdsValueOf == newObj)
            return;
        else {
            // removing old variable
            Obj oldObj = register.holdsValueOf;
            AddressDescriptor oldObjDescriptor = addressDescriptors.get(oldObj);
            if (oldObj != null && oldObjDescriptor.getDescriptor() instanceof RegisterDescriptor) {
                out.add("\tMOV " + oldObjDescriptor.getMemoryDescriptor() + ", " + oldObjDescriptor.getDescriptor());
                dirtyVariables.remove(oldObj);

                assert oldObjDescriptor.getDescriptor() == register;
                oldObjDescriptor.setRegisterLocation(null);
            }

            if (newObj.getKind() == Obj.Con) {
                int size = SystemV_ABI.getX64VariableSize(newObj.getType());
                out.add("\tMOV " + register.getNameBySize(size) + ", " + newObj.getAdr());
                return;
            }

            // loading new variable
            AddressDescriptor newObjDescriptor = addressDescriptors.get(newObj);
            register.setPrintWidth(SystemV_ABI.getX64VariableSize(newObj.getType()));
            out.add("\tMOV " + register + ", " + addressDescriptors.get(newObj).getDescriptor());
        }
    }

    public RegisterDescriptor getRegister(Obj obj, Quadruple instruction) {
        return getRegister(obj, instruction, new ArrayList<>());
    }

    public RegisterDescriptor getRegister(Obj obj, Quadruple instruction, List<RegisterDescriptor> reservedRegisters) {
        AddressDescriptor addressDescriptor = addressDescriptors.get(obj);

        if (addressDescriptor != null &&
                addressDescriptor.getDescriptor() instanceof RegisterDescriptor &&
                !reservedRegisters.contains(addressDescriptor.getDescriptor())) {
            // CASE: obj is already in register, no action needed
            return (RegisterDescriptor) addressDescriptor.getDescriptor();
        } else if ((addressDescriptor == null || addressDescriptor.getDescriptor() instanceof MemoryDescriptor) &&
                freeRegisters.size() > 0) {
            // CASE: var not encountered before or is not in a register and there are free registers
            // take a register from the list and return
            RegisterDescriptor register = freeRegisters.get(0);
            if (reservedRegisters == null || !reservedRegisters.contains(register)) {
                freeRegisters.remove(register);

                return register;
            }
        }

            /*RegisterDescriptor duplicate = getRegisterIfDuplicated();
            if (duplicate != null)
                return duplicate;*/

            /*RegisterDescriptor twoAppearancesInSingleInstruction = getRegisterIfDestination(instruction);
            if (twoAppearancesInSingleInstruction != null)
                return twoAppearancesInSingleInstruction;

            RegisterDescriptor livenessRegister = getRegisterByLiveness(instruction);
            if (livenessRegister != null)
                return livenessRegister;*/

        // return any register
        int numberOfTries = 0;
        int circularAllocation = 0;

        if (circularAllocation >= allRegisters.size())
            circularAllocation = 0;

        RegisterDescriptor reg = null;
        while (numberOfTries < allRegisters.size()) {
            reg = allRegisters.get(circularAllocation++);
            numberOfTries++;

            if (circularAllocation >= allRegisters.size())
                circularAllocation = 0;

            if (!reservedRegisters.contains(reg))
                break;
        }

        if (reg == null || numberOfTries == allRegisters.size())
            throw new RuntimeException("Unable to allocate register.");

        return reg;
    }

    /**
     * If v is not used later (that is, after the instruction I, there
     * are no further uses of v, and if v is live on exit from the block, then
     * v is recomputed within the block).
     *
     * @return Register descriptor if condition is satisfied, otherwise null.
     */
    /*private RegisterDescriptor getRegisterByLiveness(Quadruple instruction) {
        Obj obj1 = (instruction.getArg1() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) instruction.getArg1()).getObj() : null);
        Obj obj2 = (instruction.getArg2() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) instruction.getArg2()).getObj() : null);
        Obj objResult = (instruction.getResult() instanceof QuadrupleObjVar ? ((QuadrupleObjVar) instruction.getResult()).getObj() : null);

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
    }*/

    /**
     * If v is x, the variable being computed by instruction I, and x is not
     * also one of the other operands of instruction I (z in this example),
     * then we are OK. The reason is that in this case, we know this value
     * of x is never again going to be used, so we are free to ignore it.
     *
     * @param instruction Quadruple instruction.
     * @return Register descriptor if condition is satisfied, otherwise null.
     */
    /*private RegisterDescriptor getRegisterIfDestination(Quadruple instruction) {
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
    }*/

    /**
     * Instruction is x = y + z. Prospective register is R that holds v obj node.
     * If multiple registers hold v, than return R.
     *
     * @return Register descriptor if condition is satisfied, otherwise null.
     */
    /*private RegisterDescriptor getRegisterIfDuplicated() {
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

        if (duplicates.size() > 0 && duplicates.firstKey() > 1)
            return duplicates.get(duplicates.firstKey());
        else
            return null;
    }*/

    /**
     * This method is invoked on end of basic block's code generation
     * in order to save all non-temporary variables to memory that
     * were made dirty during the execution
     */
    public void saveDirtyVariablesAndClearAddressDescriptors(List<String> out, boolean saveTemps) {
        for (Obj obj : dirtyVariables) {
            if (obj == null || !saveTemps && obj.tempVar)
                continue;

            AddressDescriptor addressDescriptor = addressDescriptors.get(obj);

            if (addressDescriptor.getDescriptor() instanceof RegisterDescriptor) {
                out.add("\tMOV " + addressDescriptor.getMemoryDescriptor() + ", " + addressDescriptor.getDescriptor().toString());
                //((RegisterDescriptor)addressDescriptor.getDescriptor()).setHoldsValueOf(null);
                addressDescriptor.setRegisterLocation(null);
            }
        }
        dirtyVariables.clear();

        for (Obj obj : addressDescriptors.keySet()) {
            AddressDescriptor addressDescriptor = addressDescriptors.get(obj);
            if (addressDescriptor.getDescriptor() instanceof RegisterDescriptor)
                addressDescriptor.setRegisterLocation(null);
        }

    }

    public RegisterDescriptor getRegisterByName(String _64_bit_name) {
        RegisterDescriptor res = _64_bit_name_to_reg_descriptor.get(_64_bit_name);
        if (res == null)
            throw new RuntimeException("Required register doesn't exist.");

        return res;
    }

    /**
     * Checks whether register is free
     *
     * @param descriptor
     * @return
     */
    public boolean checkIfRegisterIsTaken(RegisterDescriptor descriptor) {
        for (AddressDescriptor addressDescriptor : addressDescriptors.values()) {
            if (addressDescriptor.getDescriptor() == descriptor)
                return true;
        }

        return false;
    }

    //////////////////////////////////////// CONTEXT PRESERVATION

    /*
    Only RBP, RBX, R12-R15 should be saved by callee -> ABI 3.2.1
     */

    public void preserveContext(List<RegisterDescriptor> usedRegisters, List<String> out) {
        // push operations not allowed -> store required registers in memory
        for (int i = 0; i < usedRegisters.size(); i++) {
            RegisterDescriptor descriptor = usedRegisters.get(i);
            if (descriptor.holdsValueOf == null)
                continue;
            else if (descriptor.holdsValueOf.getKind() == Obj.Con)
                continue;

            out.add("\tMOV " + addressDescriptors.get(descriptor.holdsValueOf).getMemoryDescriptor() + ", " + descriptor);
            dirtyVariables.remove(descriptor.holdsValueOf);
        }
    }

    public void restoreContext(List<RegisterDescriptor> usedRegisters, List<String> out) {
        for (int i = usedRegisters.size() - 1; i >= 0; i--) {
            RegisterDescriptor descriptor = usedRegisters.get(i);
            if (descriptor.holdsValueOf == null)
                continue;

            out.add("\tMOV " + descriptor + ", " +
                    (descriptor.holdsValueOf.getKind() != Obj.Con ? addressDescriptors.get(descriptor.holdsValueOf).getMemoryDescriptor() : descriptor.holdsValueOf.getAdr()));
        }
    }

    public void saveParametersToMemoryLocations(Obj function, BufferedWriter writer) throws IOException {
        if (function.getKind() == Obj.Meth) {
            for (Obj var : function.getLocalSymbols()) {
                if (var.parameter && var.parameterDescriptor != null) {
                    writer.write("\tMOV " + SystemV_ABI.getPtrSpecifier(var.getType()) + " [RBP - " + var.getAdr() + "], " + var.parameterDescriptor.getNameBySize(SystemV_ABI.getX64VariableSize(var.getType())));
                    writer.write(System.lineSeparator());

                    //AddressDescriptor addressDescriptor = addressDescriptors.get(var);
                    /*
                    addressDescriptor.setRegisterLocation(var.parameterDescriptor);

                    var.parameterDescriptor.holdsValueOf = var;
                    freeRegisters.remove(var.parameterDescriptor);*/
                }
            }
        } else
            throw new RuntimeException("Invalid parameter.");
    }

    public void saveRegisterFile(List<String> out) {
        out.add("\tpushq rbx");
        out.add("\tpushq r12");
        out.add("\tpushq r13");
        out.add("\tpushq r14");
        out.add("\tpushq r15");
        out.add("\tsub rsp, 24");
        out.add(System.lineSeparator());
    }

    public void restoreRegisterFile(List<String> out) {
        out.add(System.lineSeparator());
        out.add("\tadd rsp, 24");
        out.add("\tpopq r15");
        out.add("\tpopq r14");
        out.add("\tpopq r13");
        out.add("\tpopq r12");
        out.add("\tpopq rbx");
    }

    public void invalidateRegisters() {
        for (RegisterDescriptor registerDescriptor : allRegisters) {
            registerDescriptor.setHoldsValueOf(null);
            invalidateAddressDescriptors(registerDescriptor.ISA_8_ByteName);
            if (!freeRegisters.contains(registerDescriptor))
                freeRegisters.add(registerDescriptor);
        }
    }

    public void invalidateAddressDescriptors(String name) {
        for (AddressDescriptor descriptor : addressDescriptors.values()) {
            if (descriptor.getDescriptor() instanceof RegisterDescriptor && (((RegisterDescriptor) descriptor.getDescriptor()).ISA_8_ByteName.equals(name)))
                descriptor.setRegisterLocation(null);
        }
    }
}
