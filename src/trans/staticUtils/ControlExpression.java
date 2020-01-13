package trans.staticUtils;

import java.util.ArrayList;

public class ControlExpression extends StaticMsg {
    private ArrayList<VarNode> _useList = new ArrayList<>();

    ControlExpression(int line, int column) {
        super(line, column);
    }

    void addUse(VarNode var) {
        _useList.add(var);
    }

    public ArrayList<VarNode> getUseList() {
        return _useList;
    }
}
