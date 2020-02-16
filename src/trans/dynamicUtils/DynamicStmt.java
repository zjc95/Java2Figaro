package trans.dynamicUtils;

import trans.common.LevelLogger;
import trans.staticUtils.Stmt;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DynamicStmt extends DynamicMsg {
    private DynamicStmt _structure = null;
    private Map<String, String> _useIDMap = new HashMap<>();
    private ArrayList<DynamicCtrlExpr> _ctrlList = new ArrayList<>();
    private ArrayList<FieldRelation> _relationList = new ArrayList<>();

    DynamicStmt(DynamicInfo info, int line, int column, Stmt stmt) {
        super(info, line, column, stmt);
    }

    private DynamicStmt getStructure(Stmt stmt) {
        Stmt structure = stmt.getStructure();
        if (structure != null) {
            DynamicStmt dycStructure = _info.getStructure(structure);
            if (dycStructure == null)
                LevelLogger.error("ERROR : Structure Not Found : line " + getLine() + " , column " + getColumn());
            return dycStructure;
        }
        return null;
    }

    void parse() {
        Stmt stmt = (Stmt) _msg;
        _figaroID = _info.genStmtFigaroID(this);
        for (VarNode var : stmt.getUseList()) {
            FieldRelation relation = _info.genFieldRelation(var);
            if (relation != null) _relationList.add(relation);
            _useIDMap.put(var.getID(), _info.genVarFigaroID(var, false));
        }
        if (stmt.isControlStmt())
            _ctrlList = _info.getCtrlList();

        _structure = getStructure(stmt);
        _info.setStructure(stmt, this);
    }

    String genSource() {
        StringBuilder source = new StringBuilder();
        for (FieldRelation relation : _relationList)
            source.append(DynamicInfo.genDefinitionSource(relation.getDef(), relation.getUse()));

        ArrayList<String> useList = new ArrayList<>();
        Stmt stmt = (Stmt) _msg;
        if (!stmt.isControlStmt())
            useList.addAll(_useIDMap.values());
        else
            for (DynamicCtrlExpr ctrl : _ctrlList)
                useList.add(ctrl.getFigaroID());
        if(_structure != null)
            useList.add(_structure.getFigaroID());
        source.append(DynamicInfo.genDefinitionSource(_figaroID, useList));

        return source.toString();
    }
}
