package Trans.StaticUtils;

public class StaticMsg {
    private int _column;
    private int _line;

    StaticMsg(int line, int column) {
        _line = line;
        _column = column;
    }

    int getLine() {
        return _line;
    }

    int getColumn() {
        return  _column;
    }
}
