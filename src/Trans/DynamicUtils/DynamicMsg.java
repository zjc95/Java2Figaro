package Trans.DynamicUtils;

import Trans.StaticUtils.StaticMsg;

public class DynamicMsg {
    private int _column;
    private int _line;
    private StaticMsg _msg;

    DynamicMsg(int line, int column, StaticMsg msg) {
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
}
