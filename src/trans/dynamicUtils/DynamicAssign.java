package trans.dynamicUtils;

import trans.common.LevelLogger;
import trans.common.Util;
import trans.staticUtils.Assign;
import trans.staticUtils.Stmt;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DynamicAssign extends DynamicMsg {
    private String _value;
    private DynamicStmt _structure = null;
    private Map<String, String> _useIDMap = new HashMap<>();
    private ArrayList<FieldRelation> _relationList = new ArrayList<>();

    DynamicAssign(DynamicInfo info, int line, int column, Assign assign, String value) {
        super(info, line, column, assign);
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    public VarNode getDefVar() {
        if (_msg == null)
            return null;

        Assign assign = (Assign) _msg;
        return assign.getDef();
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
        Assign assign = (Assign) _msg;
        for (VarNode var : assign.getUseList()) {
            FieldRelation relation = _info.genFieldRelation(var);
            if (relation != null) _relationList.add(relation);

            String varFigaroID = _info.genVarFigaroID(var, false);
            if (varFigaroID != null)
                _useIDMap.put(var.getID(), varFigaroID);
        }
        _figaroID = _info.genVarFigaroID(assign.getDef(), true);
        _info.addFieldRelation(assign.getDef());
        _structure = getStructure(assign.getStmt());
        if (_structure != null)
            _info.addStructureDefine(_structure, assign.getDef());
    }

    public ArrayList<String> getUseList() {
        ArrayList<String> useList = new ArrayList<>(_useIDMap.values());
        if (_structure != null)
            useList.add(_structure.getFigaroID());
        useList.addAll(_info.getConstraintList(_figaroID));
        return useList;
    }

    String genSource() {
        StringBuilder source = new StringBuilder();
        for (FieldRelation relation : _relationList)
            source.append(DynamicInfo.genDefinitionSource(relation.getDef(), relation.getUse(), Util.SEMANTIC_LOW_PROBABILITY));
        source.append(DynamicInfo.genDefinitionSource(_figaroID, getUseList(), Util.SEMANTIC_LOW_PROBABILITY));
        return source.toString();
    }
}
