package trans.dynamicUtils;

import trans.staticUtils.Assign;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DynamicAssign extends DynamicMsg {
    private String _value;
    private DynamicStmt _structure = null;
    private Map<String, String> _useFigaroID = new HashMap<>();

    DynamicAssign(DynamicInfo info, int line, int column, Assign assign, String value) {
        super(info, line, column, assign);
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    void parse() {
        Assign assign = (Assign) _msg;
        for (VarNode var : assign.getUse())
            _useFigaroID.put(var.getName(), _info.genVarFigaroID(var, false));
        _figaroID = _info.genVarFigaroID(assign.getDef(), true);
        _structure = getStructure(assign.getStmt());
    }

    String genSource() {
        ArrayList<String> useList = new ArrayList<>(_useFigaroID.values());
        if (_structure != null)
            useList.add(_structure.getFigaroID());
        return genDefinitionSource(useList);
    }
}
