package trans.staticUtils;

import org.eclipse.jdt.core.dom.ASTNode;

public class StaticMsg {
    private int _column;
    private int _line;
    private ASTNode _node;

    StaticMsg(int line, int column, ASTNode node) {
        _line = line;
        _column = column;
        _node = node;
    }

    public ASTNode getNode() {
        return _node;
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
