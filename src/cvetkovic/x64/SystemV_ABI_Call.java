package cvetkovic.x64;

import cvetkovic.x64.cpu.RegisterDescriptor;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Buffer for function call parameters. Generates parameter passing code
 */
public class SystemV_ABI_Call {
    private List<Obj> arguments = new ArrayList<>();
    private ResourceManager resourceManager;

    public SystemV_ABI_Call(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void putParameter(Obj obj) {
        arguments.add(obj);
    }

    public List<String> generateCallForParameters(Obj function) {
        List<String> instructions = new ArrayList<>();

        List<Obj> iterator = function.getLocalSymbols().stream().filter(p -> p.parameter).collect(Collectors.toList());
        int stackOffset = 16;
        for (int i = 0; i < iterator.size(); i++) {
            RegisterDescriptor destination = getParameterRegister(i);
            Obj parameterVar = iterator.get(i);

            // TODO: set addresses of parameters in symbol table
            if (destination != null) {
                parameterVar.parameterDescriptor = destination;
            }
            else {
                parameterVar.stackParameter = true;
                parameterVar.setAdr(stackOffset);
                stackOffset += 8;
                //throw new RuntimeException("Argument stacking not yet implemented.");
            }
        }

        return instructions;
    }

    /**
     * Returns register where the parameter should be placed
     *
     * @param orderNumber Order number of parameter
     * @return Register descriptor if the parameter with provided order number should be placed in register.
     * Otherwise null is returned, which means that is should be placed on stack.
     */
    public RegisterDescriptor getParameterRegister(int orderNumber) {
        switch (orderNumber) {
            case 0:
                return resourceManager.getRegisterByName("rdi");
            case 1:
                return resourceManager.getRegisterByName("rsi");
            case 2:
                return resourceManager.getRegisterByName("rdx");
            case 3:
                return resourceManager.getRegisterByName("rcx");
            case 4:
                return resourceManager.getRegisterByName("r8");
            case 5:
                return resourceManager.getRegisterByName("r9");
            default:
                return null;
        }
    }
}
