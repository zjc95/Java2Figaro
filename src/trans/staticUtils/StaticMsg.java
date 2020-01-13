package trans.staticUtils;

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

    public String getKey() {
        return getKey(_line, _column);
    }

    static String getKey(int line, int column) {
        return line + "," + column;
    }
}
