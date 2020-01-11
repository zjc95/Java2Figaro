package Trans.demo;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Stmt {
    private int _line;
    private int _column;
    private Stmt _structure;
    private ASTNode _node;
    private ArrayList<Assign> _assign = new ArrayList<>();
    private ArrayList<ControlExpression> _controlExpr = new ArrayList<>();
    private Map<String, VarNode> _use = new HashMap<>();

    Stmt(ASTNode node, Stmt structure, int line, int column) {
        _node = node;
        _structure = structure;
        _line = line;
        _column = column;
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

    void addUse(VarNode use) {
        String varName = use.getName();
        if (!_use.containsKey(varName))
            _use.put(varName, use);
    }

    ArrayList<VarNode> getUse() {
        return new ArrayList<>(_use.values());
    }

    int getLine() {
        return _line;
    }

    int getColumn() {
        return  _column;
    }
}
