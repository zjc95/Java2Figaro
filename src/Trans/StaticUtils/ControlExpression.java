package Trans.StaticUtils;

import java.util.ArrayList;

class ControlExpression extends StaticMsg {
    private ArrayList<VarNode> _use = new ArrayList<>();

    ControlExpression(int line, int column) {
        super(line, column);
    }

    void addUse(VarNode var) {
        _use.add(var);
    }

    ArrayList<VarNode> getUse() {
        return _use;
    }
}
