package trans.staticUtils;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StaticInfo {
    private ArrayList<Stmt> _stmtList = new ArrayList<>();
    private Map<String, VarNode> _varMap = new HashMap<>();
    private Map<String, Stmt> _stmtMap = new HashMap<>();
    private Map<String, Assign> _assignMap = new HashMap<>();
    private Map<String, ControlExpression> _ctrlExprMap = new HashMap<>();

    void build() {
        //---------------Statement Map----------------
        for (Stmt stmt : _stmtList)
            _stmtMap.put(stmt.getLine() + "," + stmt.getColumn(), stmt);

        //----------------Assign Map-----------------
        for (Stmt stmt : _stmtList)
            for (Assign assign : stmt.getAssign())
                _assignMap.put(assign.getLine() + "," + assign.getColumn(), assign);

        //----------Control Expression Map------------
        for (Stmt stmt : _stmtList)
            for (ControlExpression expr : stmt.getControlExpr())
                _ctrlExprMap.put(expr.getLine() + "," + expr.getColumn(), expr);
    }

    VarNode addVar(String varID) {
        String varName = VarNode.transID2Name(varID);
        if (_varMap.containsKey(varName))
            return _varMap.get(varName);
        VarNode var = new VarNode(varName);
        _varMap.put(varName, var);
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

    void addStatement(Stmt stmt) {
        _stmtList.add(stmt);
    }

    String AnalyzeInformation() {
        ArrayList<VarNode> entryList = new ArrayList<>();
        Map<String, VarNode> retList = new HashMap<>();

        //----------Return List------------
        for(Stmt stmt : _stmtList)
            if (stmt.getNode() instanceof ReturnStatement)
                for(VarNode var : stmt.getUse())
                    if (!retList.containsKey(var.getName()))
                        retList.put(var.getName(), var);

        //----------Entry List------------
        for(Stmt stmt : _stmtList)
            if (stmt.getNode() instanceof MethodDeclaration)
                for (Assign assign : stmt.getAssign())
                    entryList.add(assign.getDef());
                
        StringBuilder info = new StringBuilder("Static Analyze Information\n");
        info.append("Entry Variables: ");
        for (VarNode var : entryList)
            info.append(var.getName()).append(" ");
        info.append("\n");

        info.append("Return Variables: ");
        for (VarNode var : retList.values())
            info.append(var.getName()).append(" ");
        info.append("\n");

        for (Stmt stmt : _stmtList) {
            info.append("Stmt(").append(stmt.getLine()).append(",").append(stmt.getColumn()).append("):   ");

            for (Assign assign : stmt.getAssign()) {
                info.append("Assign(").append(assign.getLine()).append(",").append(assign.getColumn()).append(") : ");
                info.append("Def[ ").append(assign.getDef().getName()).append(" ] ");

                info.append("Use[ ");
                ArrayList<VarNode> useList = assign.getUse();
                for (VarNode var : useList)
                    info.append(var.getName()).append(" ");
                info.append("] ; ");
            }

            for (ControlExpression expr : stmt.getControlExpr()) {
                info.append("Control(").append(expr.getLine()).append(",").append(expr.getColumn()).append(") : ");
                info.append("Use[ ");
                ArrayList<VarNode> useList = expr.getUse();
                for (VarNode var : useList)
                    info.append(var.getName()).append(" ");
                info.append("] ; ");
            }
            info.append("\n");
        }
        info.append("-----------Information End---------------");
        return info.toString();
    }
}
