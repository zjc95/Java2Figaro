package trans.staticUtils;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

import java.util.ArrayList;
import java.util.Arrays;
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
            _stmtMap.put(stmt.getKey(), stmt);

        //----------------Assign Map-----------------
        for (Stmt stmt : _stmtList)
            for (Assign assign : stmt.getAssignList())
                _assignMap.put(assign.getKey(), assign);

        //----------Control Expression Map------------
        for (Stmt stmt : _stmtList)
            for (ControlExpression expr : stmt.getControlExprList())
                _ctrlExprMap.put(expr.getKey(), expr);

        //----------Variable Relationship-------------
        for (VarNode field : _varMap.values())
            for (VarNode subject : _varMap.values())
                if (checkField(field, subject)) {
                    field.addSubject(subject);
                    subject.addField(field);
                }
    }

    VarNode addVar(String varID) {
        if (_varMap.containsKey(varID))
            return _varMap.get(varID);
        VarNode var = new VarNode(varID);
        _varMap.put(varID, var);
        return var;
    }

    public Stmt getStmt(int line, int column) {
        return _stmtMap.get(StaticMsg.getKey(line, column));
    }

    public Assign getAssign(int line, int column) {
        return _assignMap.get(StaticMsg.getKey(line, column));
    }

    public ControlExpression getCtrlExpr(int line, int column) {
        return _ctrlExprMap.get(StaticMsg.getKey(line, column));
    }

    private boolean checkField(VarNode field, VarNode subject) {
        ArrayList<String> fieldList = new ArrayList<>(Arrays.asList(field.getID().split("\\.")));
        ArrayList<String> subjectList = new ArrayList<>(Arrays.asList(subject.getID().split("\\.")));

        //System.out.println(field.getID() + " " + subject.getID());
        //System.out.println(fieldList);
        //System.out.println(subjectList);

        if (fieldList.size() <= subjectList.size())
            return false;
        for (int i = 0; i < subjectList.size(); i++)
            if (!fieldList.get(i).equals(subjectList.get(i)))
                return false;
        return true;
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
                for(VarNode var : stmt.getUseList())
                    if (!retList.containsKey(var.getName()))
                        retList.put(var.getName(), var);

        //----------Entry List------------
        for(Stmt stmt : _stmtList)
            if (stmt.getNode() instanceof MethodDeclaration)
                for (Assign assign : stmt.getAssignList())
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

        info.append("Field Relation: \n");
        for (VarNode var : _varMap.values())
        {
            ArrayList<VarNode> fieldList = var.getField();
            if (fieldList.size() == 0) continue;
            info.append("Var ").append(var.getID()).append(" : ");
            for (VarNode field : fieldList)
                info.append(field.getID()).append(" ");
            info.append("\n");
        }

        for (Stmt stmt : _stmtList) {
            info.append("Stmt(").append(stmt.getLine()).append(",").append(stmt.getColumn()).append("):   ");

            for (Assign assign : stmt.getAssignList()) {
                info.append("Assign(").append(assign.getLine()).append(",").append(assign.getColumn()).append(") : ");
                info.append("Def[ ").append(assign.getDef().getName()).append(" ] ");

                info.append("Use[ ");
                ArrayList<VarNode> useList = assign.getUseList();
                for (VarNode var : useList)
                    info.append(var.getName()).append(" ");
                info.append("] ; ");
            }

            for (ControlExpression expr : stmt.getControlExprList()) {
                info.append("Control(").append(expr.getLine()).append(",").append(expr.getColumn()).append(") : ");
                info.append("Use[ ");
                ArrayList<VarNode> useList = expr.getUseList();
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
