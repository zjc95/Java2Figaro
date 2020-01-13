package trans.dynamicUtils;

import trans.staticUtils.Stmt;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DynamicRet extends DynamicMsg {
    private String _value;
    private DynamicStmt _structure = null;
    private Map<String, String> _useIDMap = new HashMap<>();

    DynamicRet(DynamicInfo info, int line, int column, Stmt stmt, String value) {
        super(info, line, column, stmt);
        _value = value;
        _figaroID = "Ret";
    }

    void parse() {
        Stmt stmt = (Stmt) _msg;
        for (VarNode var : stmt.getUseList())
            _useIDMap.put(var.getID(), _info.genVarFigaroID(var, false));
        _structure = getStructure(stmt);
    }

    String genSource() {
        ArrayList<String> useList = new ArrayList<>(_useIDMap.values());
        if (_structure != null)
            useList.add(_structure.getFigaroID());
        return genDefinitionSource(useList);
    }
}
