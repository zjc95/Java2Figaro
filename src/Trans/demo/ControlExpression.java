package Trans.demo;

import java.util.ArrayList;

class ControlExpression {
    private int _line;
    private int _column;
    private ArrayList<VarNode> _use = new ArrayList<>();

    ControlExpression(int line, int column) {
        _line = line;
        _column = column;
    }

    void addUse(VarNode var) {
        _use.add(var);
    }

    ArrayList<VarNode> getUse() {
        return _use;
    }

    int getLine() {
        return _line;
    }

    int getColumn() {
        return  _column;
    }
}
