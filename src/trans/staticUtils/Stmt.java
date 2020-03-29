package trans.staticUtils;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Stmt extends StaticMsg {
    private Stmt _structure;
    private ArrayList<Assign> _assignList = new ArrayList<>();
    private ArrayList<Assign> _scopeDefineList = new ArrayList<>();
    private ArrayList<ControlExpression> _controlExprList = new ArrayList<>();
    private ArrayList<Stmt> _controlStmtList = new ArrayList<>();
    private Map<String, VarNode> _useList = new HashMap<>();

    Stmt(ASTNode node, Stmt structure, int line, int column) {
        super(line, column, node);
        _structure = structure;
        if (structure != null)
            structure.addControlStmt(this);
    }

    void addAssign(Assign assign) {
        _assignList.add(assign);
        if (_structure != null)
            _structure.addScopeDefine(assign);
    }

    ArrayList<Assign> getAssignList() {
        return _assignList;
    }

    void addScopeDefine(Assign assign) {
        _scopeDefineList.add(assign);
    }

    void addControlStmt(Stmt stmt) {
        _controlStmtList.add(stmt);
    }

    public ArrayList<Stmt> getControlStmtList() {
        return _controlStmtList;
    }

    public HashSet<String> getScopeDefine() {
        HashSet<String> scopeDefineSet = new HashSet<>();
        for (Assign assign : _scopeDefineList)
            scopeDefineSet.add(assign.getDef().getID());
        return scopeDefineSet;
    }

    void addControlExpr(ControlExpression expr) {
        _controlExprList.add(expr);
    }

    public ArrayList<ControlExpression> getControlExprList() {
        return _controlExprList;
    }

    public boolean isControlStmt() {
        return _controlExprList.size() > 0;
    }

    void addUse(VarNode use) {
        String varID = use.getID();
        if (!_useList.containsKey(varID))
            _useList.put(varID, use);
    }

    public ArrayList<VarNode> getUseList() {
        return new ArrayList<>(_useList.values());
    }

    public Stmt getStructure() {
        return _structure;
    }
}
