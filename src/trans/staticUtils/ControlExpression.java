package trans.staticUtils;

import java.util.ArrayList;

public class ControlExpression extends StaticMsg {
    private ArrayList<VarNode> _use = new ArrayList<>();

    ControlExpression(int line, int column) {
        super(line, column);
    }

    void addUse(VarNode var) {
        _use.add(var);
    }

    public ArrayList<VarNode> getUse() {
        return _use;
    }
}
