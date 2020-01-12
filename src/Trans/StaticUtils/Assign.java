package Trans.StaticUtils;

import java.util.ArrayList;

public class Assign extends StaticMsg {
    private VarNode _def;
    private ArrayList<VarNode> _use = new ArrayList<>();

    Assign(Stmt stmt, int line, int column) {
        super(line, column);
        stmt.addAssign(this);
    }

    void setDef(VarNode def) {
        _def = def;
    }

    public VarNode getDef() {
        return _def;
    }

    void addUse(VarNode use) {
        _use.add(use);
    }

    public ArrayList<VarNode> getUse() {
        return _use;
    }
}
