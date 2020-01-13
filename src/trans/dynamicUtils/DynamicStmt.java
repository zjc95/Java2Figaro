package trans.dynamicUtils;

import trans.staticUtils.Stmt;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DynamicStmt extends DynamicMsg {
    private DynamicStmt _structure = null;
    private Map<String, String> _useFigaroID = new HashMap<>();
    private ArrayList<DynamicCtrlExpr> _ctrlList = new ArrayList<>();

    DynamicStmt(DynamicInfo info, int line, int column, Stmt stmt) {
        super(info, line, column, stmt);
    }

    void parse() {
        Stmt stmt = (Stmt) _msg;
        _figaroID = _info.genStmtFigaroID(this);
        for (VarNode var : stmt.getUse())
            _useFigaroID.put(var.getName(), _info.genVarFigaroID(var, false));
        if (stmt.isControlStmt())
            _ctrlList = _info.getCtrlList();

        _structure = getStructure(stmt);
        _info.setStructure(stmt, this);
    }

    String genSource() {
        ArrayList<String> useList = new ArrayList<>();
        Stmt stmt = (Stmt) _msg;
        if (!stmt.isControlStmt())
            useList.addAll(_useFigaroID.values());
        else
            for (DynamicCtrlExpr ctrl : _ctrlList)
                useList.add(ctrl.getFigaroID());

        if(_structure != null)
            useList.add(_structure.getFigaroID());

        return genDefinitionSource(useList);
    }
}
