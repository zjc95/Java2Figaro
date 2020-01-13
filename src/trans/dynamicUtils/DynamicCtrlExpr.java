package trans.dynamicUtils;

import trans.staticUtils.ControlExpression;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DynamicCtrlExpr extends DynamicMsg {
    private boolean _value;
    private Map<String, String> _useIDMap = new HashMap<>();

    DynamicCtrlExpr(DynamicInfo info, int line, int column, ControlExpression expr, boolean value) {
        super(info, line, column, expr);
        _value = value;
    }

    void parse() {
        ControlExpression ctrl = (ControlExpression) _msg;
        _figaroID = _info.genCtrlFigaroID(this);
        for (VarNode var : ctrl.getUseList())
            _useIDMap.put(var.getID(), _info.genVarFigaroID(var, false));
        _info.addCtrlExpr(this);
    }

    String genSource() {
        return genDefinitionSource(new ArrayList<>(_useIDMap.values()));
    }
}
