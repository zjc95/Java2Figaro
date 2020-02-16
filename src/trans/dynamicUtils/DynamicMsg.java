package trans.dynamicUtils;

import trans.staticUtils.StaticMsg;

public class DynamicMsg {
    private int _column;
    private int _line;
    protected StaticMsg _msg;
    protected DynamicInfo _info;
    protected String _figaroID = null;

    DynamicMsg(DynamicInfo info, int line, int column, StaticMsg msg) {
        _info = info;
        _line = line;
        _column = column;
        _msg = msg;
    }

    int getLine() {
        return _line;
    }

    int getColumn() {
        return  _column;
    }

    String getKey() {
        return _line + "_" + _column;
    }

    void parse() {}

    String genSource() {
        return null;
    }

    public String getFigaroID() { return _figaroID; }
}
