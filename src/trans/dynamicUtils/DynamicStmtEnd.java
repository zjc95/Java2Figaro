package trans.dynamicUtils;

import javafx.util.Pair;
import org.eclipse.jdt.core.dom.IfStatement;
import trans.common.LevelLogger;
import trans.common.Util;
import trans.staticUtils.Stmt;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashSet;

public class DynamicStmtEnd extends DynamicMsg {
    private DynamicStmt _structure = null;
    private DynamicStmt _scope = null;
    private ArrayList<Pair<String, String>> _scopeUnusedList = new ArrayList<>();

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
        Stmt stmt = (Stmt) _msg;
        _scope = _info.getStructure(stmt);
        _figaroID = _scope.getFigaroID();
        _structure = getStructure();
        if (stmt.getNode() instanceof IfStatement) {
            HashSet<String> scopeUnusedSet = stmt.getScopeDefine();
            HashSet<String> scopeDefSet = _info.getStructureDefine(_scope);
            scopeUnusedSet.removeAll(scopeDefSet);
            //System.out.println(stmt.getNode().toString());
            for (String varID : scopeUnusedSet) {
                //System.out.print(varID + " ");
                String varUseID = _info.genVarFigaroID(new VarNode(varID), false);
                String varDefID = _info.genVarFigaroID(new VarNode(varID), true);
                _scopeUnusedList.add(new Pair<>(varDefID, varUseID));
            }
            //System.out.println("\n--------------");
            _info.addStructureDefine(_structure, scopeDefSet);
        }
    }

    public ArrayList<Pair<String, String>> getScopeDefList() {
        return _scopeUnusedList;
    }

    String genSource() {
        StringBuilder source = new StringBuilder();
        for (Pair<String, String> pair : _scopeUnusedList) {
            ArrayList<String> useList = new ArrayList<>();
            useList.add(pair.getValue());
            useList.add(_figaroID);
            source.append(DynamicInfo.genDefinitionSource(pair.getKey(), useList, Util.SEMANTIC_LOW_PROBABILITY));
        }
        //System.out.println(source);
        return source.toString();
    }
}
