package trans.dynamicUtils;

import trans.staticUtils.Stmt;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DynamicRet extends DynamicMsg {
    private String _value;
    private DynamicStmt _structure = null;
    private Map<String, String> _useFigaroID = new HashMap<>();

    DynamicRet(DynamicInfo info, int line, int column, Stmt stmt, String value) {
        super(info, line, column, stmt);
        _value = value;
        _figaroID = "Ret";
    }

    public String getValue() {
        return _value;
    }

    void parse() {
        Stmt stmt = (Stmt) _msg;
        for (VarNode var : stmt.getUse())
            _useFigaroID.put(var.getName(), _info.genVarFigaroID(var, false));
        _structure = getStructure(stmt);
    }

    String genSource() {
        ArrayList<String> useList = new ArrayList<>(_useFigaroID.values());
        if (_structure != null)
            useList.add(_structure.getFigaroID());
        return genDefinitionSource(useList);
    }
}
