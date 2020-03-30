package trans.dynamicUtils;

import trans.common.Util;
import trans.staticUtils.ControlExpression;
import trans.staticUtils.Stmt;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DynamicCtrlExpr extends DynamicMsg {
    private boolean _value;
    private Map<String, String> _useIDMap = new HashMap<>();
    private ArrayList<FieldRelation> _relationList = new ArrayList<>();

    DynamicCtrlExpr(DynamicInfo info, int line, int column, ControlExpression expr, boolean value) {
        super(info, line, column, expr);
        _value = value;
    }

    void parse() {
        ControlExpression ctrl = (ControlExpression) _msg;
        _figaroID = _info.genCtrlFigaroID(this);
        for (VarNode var : ctrl.getUseList()) {
            FieldRelation relation = _info.genFieldRelation(var);
            if (relation != null) _relationList.add(relation);

            String varFigaroID = _info.genVarFigaroID(var, false);
            if (varFigaroID != null)
                _useIDMap.put(var.getID(), varFigaroID);
        }
        _info.addCtrlExpr(this);
    }

    public ArrayList<String> getUseList() {
        ArrayList<String> useList = new ArrayList<>(_useIDMap.values());
        useList.addAll(_info.getConstraintList(_figaroID));
        return useList;
    }

    String genSource() {
        StringBuilder source = new StringBuilder();
        for (FieldRelation relation : _relationList)
            source.append(DynamicInfo.genDefinitionSource(relation.getDef(), relation.getUse(), Util.SEMANTIC_LOW_PROBABILITY));
        source.append(DynamicInfo.genDefinitionSource(_figaroID, getUseList(), Util.SEMANTIC_BOOLEAN_PROBABILITY));
        return source.toString();
    }

    String getParentKey() {
        ControlExpression controlExpression = (ControlExpression) _msg;
        return controlExpression.getParentKey();
    }
}
