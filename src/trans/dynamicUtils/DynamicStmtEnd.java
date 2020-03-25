package trans.dynamicUtils;

import javafx.util.Pair;
import org.eclipse.jdt.core.dom.IfStatement;
import trans.common.LevelLogger;
import trans.staticUtils.Stmt;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DynamicStmtEnd extends DynamicMsg {
    private DynamicStmt _structure = null;
    private ArrayList<Pair<String, String>> _scopeDefList = new ArrayList<>();

    DynamicStmtEnd(DynamicInfo info, int line, int column, Stmt stmt) {
        super(info, line, column, stmt);
    }

    private DynamicStmt getStructure() {
        Stmt stmt = (Stmt) _msg;
        Stmt structure = stmt.getStructure();
        if (structure != null) {
            DynamicStmt dycStructure = _info.getStructure(structure);
            if (dycStructure == null)
                LevelLogger.error("ERROR : Structure Not Found : line " + getLine() + " , column " + getColumn());
            return dycStructure;
        }
        return null;
    }

    void parse() {
        _figaroID = _info.getStmtFigaroID(this);
        _structure = getStructure();
        Stmt stmt = (Stmt) _msg;
        if (stmt.getNode() instanceof IfStatement) {
            HashSet<String> scopeUseSet = stmt.getScopeDefine();
            //System.out.println(stmt.getNode().toString());
            for (String varID : scopeUseSet) {
                //System.out.print(varID + " ");
                String varUseID = _info.genVarFigaroID(new VarNode(varID), false);
                String varDefID = _info.genVarFigaroID(new VarNode(varID), true);
                _scopeDefList.add(new Pair<>(varDefID, varUseID));
            }
            //System.out.println("\n--------------");
        }
    }

    public ArrayList<Pair<String, String>> getScopeDefList() {
        return _scopeDefList;
    }

    String genSource() {
        StringBuilder source = new StringBuilder();
        for (Pair<String, String> pair : _scopeDefList) {
            ArrayList<String> useList = new ArrayList<>();
            useList.add(pair.getValue());
            useList.add(_figaroID);
            source.append(DynamicInfo.genDefinitionSource(pair.getKey(), useList));
        }
        //System.out.println(source);
        return source.toString();
    }
}
