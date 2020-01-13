package trans.staticUtils;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Stmt extends StaticMsg {
    private Stmt _structure;
    private ASTNode _node;
    private ArrayList<Assign> _assign = new ArrayList<>();
    private ArrayList<ControlExpression> _controlExpr = new ArrayList<>();
    private Map<String, VarNode> _use = new HashMap<>();

    Stmt(ASTNode node, Stmt structure, int line, int column) {
        super(line, column);
        _node = node;
        _structure = structure;
    }

    ASTNode getNode() {
        return _node;
    }

    void addAssign(Assign assign) {
        _assign.add(assign);
    }

    ArrayList<Assign> getAssign() {
        return _assign;
    }

    void addControlExpr(ControlExpression expr) {
        _controlExpr.add(expr);
    }

    ArrayList<ControlExpression> getControlExpr() {
        return _controlExpr;
    }

    public boolean isControlStmt() {
        return _controlExpr.size() > 0;
    }

    void addUse(VarNode use) {
        String varName = use.getName();
        if (!_use.containsKey(varName))
            _use.put(varName, use);
    }

    public ArrayList<VarNode> getUse() {
        return new ArrayList<>(_use.values());
    }

    public Stmt getStructure() {
        return _structure;
    }
}
