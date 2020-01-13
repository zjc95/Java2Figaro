package trans.staticUtils;

import java.util.ArrayList;

public class Assign extends StaticMsg {
    private VarNode _def;
    private Stmt _stmt;
    private ArrayList<VarNode> _use = new ArrayList<>();

    Assign(Stmt stmt, int line, int column) {
        super(line, column);
        _stmt = stmt;
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

    public Stmt getStmt() {
        return _stmt;
    }
}
