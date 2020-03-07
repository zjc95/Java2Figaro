package trans.dynamicUtils;

import trans.staticUtils.Assign;
import trans.staticUtils.VarNode;

public class DynamicEntry extends DynamicMsg {
    private String _value;
    private String _exVarID;

    DynamicEntry(DynamicInfo info, int line, int column, Assign assign, String value) {
        super(info, line, column, assign);
        _value = value;
    }

    DynamicEntry(DynamicInfo info, String VarID) {
        super(info, 0, 0, null);
        _exVarID = VarID;
    }

    public String getValue() {
        return _value;
    }

    void parse() {
        if (_msg == null) {
            _figaroID = _info.genVarFigaroID(new VarNode(_exVarID), true);
        }
        else {
            Assign assign = (Assign) _msg;
            _figaroID = _info.genVarFigaroID(assign.getDef(), true);
        }
    }

    String genSource() {
        return "    val " + _figaroID + " = Flip(0.5)\n";
    }
}
