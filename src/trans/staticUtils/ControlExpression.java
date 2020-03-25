package trans.staticUtils;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;

public class ControlExpression extends StaticMsg {
    private ArrayList<VarNode> _useList = new ArrayList<>();
    private Stmt _parentStmt;

    ControlExpression(int line, int column, ASTNode node, Stmt parentStmt) {
        super(line, column, node);
        _parentStmt = parentStmt;
    }

    void addUse(VarNode var) {
        _useList.add(var);
    }

    public ArrayList<VarNode> getUseList() {
        return _useList;
    }

    public String getParentKey() {
        return _parentStmt.getKey();
    }

    public Stmt getParentStmt() { return _parentStmt; }
}
