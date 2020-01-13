package trans.dynamicUtils;

import trans.staticUtils.Assign;

class DynamicEntry extends DynamicMsg {
    private String _value;

    DynamicEntry(DynamicInfo info, int line, int column, Assign assign, String value) {
        super(info, line, column, assign);
        _value = value;
    }

    void parse() {
        Assign assign = (Assign) _msg;
        _figaroID = _info.genVarFigaroID(assign.getDef(), true);
    }

    String genSource() {
        return "    val " + _figaroID + " = Flip(0.5)\n";
    }
}
