package trans.staticUtils;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StaticInfo {
    private static int _patchNum = 0;
    private int _patchID;
    private ArrayList<VarNode> _entry = new ArrayList<>();;
    private ArrayList<Stmt> _statements = new ArrayList<>();
    private Map<String, VarNode> _vars = new HashMap<>();
    private Map<String, VarNode> _ret = new HashMap<>();
    private Map<String, Stmt> _stmtMap = new HashMap<>();
    private Map<String, Assign> _assignMap = new HashMap<>();
    private Map<String, ControlExpression> _ctrlExprMap = new HashMap<>();

    StaticInfo() {
        _patchID = ++_patchNum;
    }

    void build() {
        //----------Entry List------------
        for(Stmt stmt : _statements)
            if (stmt.getNode() instanceof MethodDeclaration)
                for (Assign assign : stmt.getAssign())
                    _entry.add(assign.getDef());

        //----------Return List------------
        for(Stmt stmt : _statements)
            if (stmt.getNode() instanceof ReturnStatement)
                for(VarNode var : stmt.getUse())
                    addReturn(var);

        //--------Statement Map-----------
        for (Stmt stmt : _statements)
            _stmtMap.put(stmt.getLine() + "," + stmt.getColumn(), stmt);

        //----------Assign Map------------
        for (Stmt stmt : _statements)
            for (Assign assign : stmt.getAssign())
                _assignMap.put(assign.getLine() + "," + assign.getColumn(), assign);

        //----------Assign Map------------
        for (Stmt stmt : _statements)
            for (ControlExpression expr : stmt.getControlExpr())
                _ctrlExprMap.put(expr.getLine() + "," + expr.getColumn(), expr);
    }

    VarNode addVar(String varID) {
        String varName = VarNode.transID2Name(varID);
        if (_vars.containsKey(varName))
            return _vars.get(varName);
        VarNode var = new VarNode(varName);
        _vars.put(varName, var);
        return var;
    }

    public Stmt getStmt(int line, int column) {
        return _stmtMap.get(line + "," + column);
    }

    public Assign getAssign(int line, int column) {
        return _assignMap.get(line + "," + column);
    }

    public ControlExpression getCtrlExpr(int line, int column) {
        return _ctrlExprMap.get(line + "," + column);
    }

    private void addReturn(VarNode var) {
        String varName = var.getName();
        if (!_ret.containsKey(varName))
            _ret.put(varName, var);
    }

    void addStatement(Stmt stmt) {
        _statements.add(stmt);
    }

    void printAnalyzeInform() {
        System.out.print("Entry Variables: ");
        for (VarNode var : _entry)
            System.out.print(var.getName() + " ");
        System.out.println();

        System.out.print("Return Variables: ");
        for (VarNode var : _ret.values())
            System.out.print(var.getName() + " ");
        System.out.println();

        for (Stmt stmt : _statements) {
            System.out.print("Stmt(" + stmt.getLine() + "," + stmt.getColumn() + "):   ");

            for (Assign assign : stmt.getAssign()) {
                System.out.print("Assign(" + assign.getLine() + "," + assign.getColumn() + ") : ");
                System.out.print("Def[ " + assign.getDef().getName() + " ] ");

                System.out.print("Use[ ");
                ArrayList<VarNode> useList = assign.getUse();
                for (VarNode var : useList)
                    System.out.print(var.getName() + " ");
                System.out.print("] ; ");
            }

            for (ControlExpression expr : stmt.getControlExpr()) {
                System.out.print("Control(" + expr.getLine() + "," + expr.getColumn() + ") : ");
                System.out.print("Use[ ");
                ArrayList<VarNode> useList = expr.getUse();
                for (VarNode var : useList)
                    System.out.print(var.getName() + " ");
                System.out.print("] ; ");
            }
            System.out.println();
        }
    }
}
