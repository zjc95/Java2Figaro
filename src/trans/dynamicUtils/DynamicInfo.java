package trans.dynamicUtils;

import trans.common.Util;
import trans.patchsim.TraceList;
import trans.staticUtils.*;
import trans.common.LevelLogger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javafx.util.Pair;
import trans.strategy.Strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DynamicInfo {
    private StaticInfo _staticInfo;
    private JSONObject _json;
    private ArrayList<String> _exEntryList = new ArrayList<>();
    private ArrayList<String> _exRetList = new ArrayList<>();

    DynamicInfo(StaticInfo stcInfo, JSONObject json) {
        _staticInfo = stcInfo;
        _json = json;
    }

    DynamicInfo(StaticInfo stcInfo, JSONObject json, ArrayList<String> exEntry, ArrayList<String> exRet) {
        _staticInfo = stcInfo;
        _json = json;
        _exEntryList.addAll(exEntry);
        _exRetList.addAll(exRet);
    }

    public TraceList genTraceList(boolean isPass) {
        return new TraceList(_msgList, isPass);
    }
    
    /***************Dynamic Inform Parse*********************/
    private ArrayList<DynamicMsg> _msgList = new ArrayList<>();
    private Map<String, DynamicStmt> _structureMap = new HashMap<>();
    private Map<String, HashSet<String>> _structureDefMap = new HashMap<>();
    private Map<String, ArrayList<Pair<VarNode, String>>> _varFieldMap = new HashMap<>();
    private Map<String, Integer> _varDefTimeMap = new HashMap<>();
    private Map<String, Integer> _stmtDefTimeMap = new HashMap<>();
    private Map<String, Integer> _controlDefTimeMap = new HashMap<>();
    private ArrayList<DynamicCtrlExpr> _controlList = new ArrayList<>();
    private ArrayList<String> _undefinedVarList = new ArrayList<>();
    private ArrayList<Pair<String, Double>> _varObservationList = new ArrayList<>();

    void parse() {
        for (String str : _exEntryList) {
            _msgList.add(new DynamicEntry(this, str));
        }

        JSONArray dataList = (JSONArray) _json.get("data");
        for (Object data : dataList) {
            JSONObject jsonMsg = (JSONObject) data;
            Long line = (Long) jsonMsg.get("line");
            Long column = (Long) jsonMsg.get("column");
            String type = (String) jsonMsg.get("type");
            Object value = jsonMsg.get("value");
            addMessage(line.intValue(), column.intValue(), type, value);
        }

        if (!(_msgList.get(_msgList.size()-1) instanceof DynamicRet)) {
            _msgList.add(new DynamicRet(this, _exRetList));
        }

        for (DynamicMsg msg : _msgList)
            msg.parse();

        Strategy stgGather = new Strategy();
        stgGather.init();
        _varObservationList = stgGather.parse(_msgList);
    }

    private void addMessage(int line, int column, String type, Object value) {
        switch (type) {
            case "ASSIGN":
                Assign assign = _staticInfo.getAssign(line, column);
                if (assign == null)
                    LevelLogger.error("JSON INFORMATION ASSIGN NOT FOUND : line " + line + ", column " + column);
                if (!(value instanceof String))
                    LevelLogger.error("JSON INFORMATION ASSIGN VALUE ERROR : line " + line + ", column " + column);
                _msgList.add(new DynamicAssign(this, line, column, assign, (String)value));
                break;
            case "ENTRY":
                Assign entry = _staticInfo.getAssign(line, column);
                if (entry == null)
                    LevelLogger.error("JSON INFORMATION ENTRY NOT FOUND : line " + line + ", column " + column);
                if (!(value instanceof String))
                    LevelLogger.error("JSON INFORMATION ENTRY VALUE ERROR : line " + line + ", column " + column);
                _msgList.add(new DynamicEntry(this, line, column, entry, (String)value));
                break;
            case "RET":
                Stmt ret = _staticInfo.getStmt(line, column);
                if (ret == null)
                    LevelLogger.error("JSON INFORMATION RET NOT FOUND : line " + line + ", column " + column);
                if (!(value instanceof String))
                    LevelLogger.error("JSON INFORMATION ASSIGN VALUE ERROR : line " + line + ", column " + column);
                _msgList.add(new DynamicRet(this, line, column, ret, (String)value, _exRetList));
                break;
            case "CONTROL":
                ControlExpression ctrl = _staticInfo.getCtrlExpr(line, column);
                if (ctrl == null)
                    LevelLogger.error("JSON INFORMATION CONTROL EXPRESSION NOT FOUND : line " + line + ", column " + column);
                if (!(value instanceof String))
                    LevelLogger.error("JSON INFORMATION CONTROL EXPRESSION VALUE ERROR : line " + line + ", column " + column);
                try {
                    boolean valueBoolean = Boolean.parseBoolean((String) value);
                    _msgList.add(new DynamicCtrlExpr(this, line, column, ctrl, valueBoolean));
                } catch (NumberFormatException e) {
                    LevelLogger.error("JSON INFORMATION CONTROL EXPRESSION VALUE ERROR : line " + line + ", column " + column);
                    e.printStackTrace();
                }
                break;
            case "STMT":
                Stmt stmt = _staticInfo.getStmt(line, column);
                if (stmt == null)
                    LevelLogger.error("JSON INFORMATION STMT NOT FOUND : line " + line + ", column " + column);
                _msgList.add(new DynamicStmt(this, line, column, stmt));
                break;
            case "END":
                Stmt stmtEnd = _staticInfo.getStmt(line, column);
                if (stmtEnd == null)
                    LevelLogger.error("JSON INFORMATION STMT NOT FOUND : line " + line + ", column " + column);
                _msgList.add(new DynamicStmtEnd(this, line, column, stmtEnd));
                break;
            default:
                LevelLogger.error("JSON INFORMATION TYPE ERROR : line " + line + ", column " + column);
        }
    }

    String genVarFigaroID(VarNode var, boolean isDef) {
        String varName = var.getName();
        String varID = var.getID();
        int varTime = 0;

        if (_varDefTimeMap.containsKey(varName)) {
            varTime = _varDefTimeMap.get(varName);
            if (isDef) {
                varTime = varTime + 1;
                _varDefTimeMap.put(varName, varTime);
            }
        }
        else {
            if (!isDef) {
                LevelLogger.warn("WARNING : Use Without Define : " + varID);
                _undefinedVarList.add("Var_" + varName);
            }
            _varDefTimeMap.put(varName, 0);
        }

        return "Var_" + varName + (varTime == 0 ? "" : "_" + varTime);
    }

    FieldRelation genFieldRelation(VarNode var) {
        if (!_varFieldMap.containsKey(var.getID()))
            return null;

        ArrayList<Pair<VarNode, String>> relationList = _varFieldMap.get(var.getID());
        if (relationList.size() == 0)
            return null;

        ArrayList<String> idList = new ArrayList<>();
        int size = relationList.size();
        for (int i = 0; i < size; i++) {
            Pair<VarNode, String> relation = relationList.get(i);
            boolean flag = true;
            for (int j = i + 1; j < size; j++)
                if (relation.getKey().checkField(relationList.get(j).getKey().getID()))
                    flag = false;
            if (flag) idList.add(relation.getValue());
        }
        relationList.clear();
        return new FieldRelation(genVarFigaroID(var, true), idList);
    }

    void addFieldRelation(VarNode var) {
        ArrayList<Pair<VarNode, String>> varRelationList = _varFieldMap.get(var.getID());
        if (varRelationList != null)
            varRelationList.clear();

        ArrayList<VarNode> subjectList = var.getSubject();
        for (VarNode subject : subjectList) {
            //System.out.println(var.getID() + " subject " + subject.getID());
            String subjectID = subject.getID();
            if (!_varFieldMap.containsKey(subjectID))
                _varFieldMap.put(subjectID, new ArrayList<>());
            ArrayList<Pair<VarNode, String>> subjectRelationList = _varFieldMap.get(subjectID);

            if (subjectRelationList.size() == 0)
                subjectRelationList.add(new Pair<>(subject, genVarFigaroID(subject, false)));
            subjectRelationList.add(new Pair<>(var, genVarFigaroID(var, false)));
        }

        ArrayList<VarNode> fieldList = var.getField();
        for (VarNode field : fieldList) {
            //System.out.println(var.getID() + " field " + field.getID());
            String fieldID = field.getID();
            if (!_varFieldMap.containsKey(fieldID))
                _varFieldMap.put(fieldID, new ArrayList<>());
            ArrayList<Pair<VarNode, String>> fieldRelationList = _varFieldMap.get(fieldID);
            fieldRelationList.add(new Pair<>(var, genVarFigaroID(var, false)));
        }
    }

    DynamicStmt getStructure(Stmt stmt) {
        return _structureMap.get(stmt.getKey());
    }

    void setStructure(Stmt stmt, DynamicStmt dynamicStmt) {
        _structureMap.put(stmt.getKey(), dynamicStmt);
        _structureDefMap.put(dynamicStmt.getFigaroID(), new HashSet<>());
    }

    void addStructureDefine(DynamicStmt structure, VarNode var) {
        HashSet<String> structureDefSet = getStructureDefine(structure);
        structureDefSet.add(var.getID());
    }

    void addStructureDefine(DynamicStmt structure, HashSet<String> defSet) {
        HashSet<String> structureDefSet = getStructureDefine(structure);
        structureDefSet.addAll(defSet);
    }

    HashSet<String> getStructureDefine(DynamicStmt structure) {
        if (structure == null)
            return new HashSet<>();
        return _structureDefMap.get(structure.getFigaroID());
    }

    void addCtrlExpr(DynamicCtrlExpr dynamicCtrlExpr) {
        if (_controlList.size() > 0) {
            DynamicCtrlExpr firstCtrlExpr = _controlList.get(0);
            if (firstCtrlExpr.getKey().equals(dynamicCtrlExpr.getKey()))
                _controlList.clear();
            if (!firstCtrlExpr.getParentKey().equals(dynamicCtrlExpr.getParentKey()))
                _controlList.clear();
        }
        _controlList.add(dynamicCtrlExpr);
    }

    ArrayList<DynamicCtrlExpr> getCtrlList() {
        ArrayList<DynamicCtrlExpr> newList = new ArrayList<>(_controlList);
        _controlList.clear();
        return newList;
    }

    String genStmtFigaroID(DynamicStmt dynamicStmt) {
        String key = dynamicStmt.getKey();
        if (_stmtDefTimeMap.containsKey(key)) {
            int ctrlTime = _stmtDefTimeMap.get(key) + 1;
            _stmtDefTimeMap.put(key, ctrlTime);
            return "Stmt_" + key + "_" + ctrlTime;
        }
        _stmtDefTimeMap.put(key, 0);
        return "Stmt_" + key;
    }

    ArrayList<String> getConstraintList(String figaroID) {
        ArrayList<String> constraintList = new ArrayList<>();
        for (int i = 0; i < _varObservationList.size(); i++) {
            Pair<String, Double> pair = _varObservationList.get(i);
            if (pair.getKey().equals(figaroID))
                constraintList.add("Constraint_" + i);
        }
        return constraintList;
    }

    String genCtrlFigaroID(DynamicCtrlExpr dynamicCtrlExpr) {
        String key = dynamicCtrlExpr.getKey();
        if (_controlDefTimeMap.containsKey(key)) {
            int ctrlTime = _controlDefTimeMap.get(key) + 1;
            _controlDefTimeMap.put(key, ctrlTime);
            return "Control_" + key + "_" + ctrlTime;
        }
        _controlDefTimeMap.put(key, 0);
        return "Control_" + key;
    }

    /***************Figaro Source Generate*********************/

    public String genFigaroSource() {
        StringBuilder _source = new StringBuilder("");

        _source.append("import com.cra.figaro.algorithm.factored.VariableElimination\n");
        _source.append("import com.cra.figaro.language._\n");
        _source.append("import com.cra.figaro.library.compound._\n\n");

        _source.append("object patch {\n");
        _source.append("  def main(args: Array[String]): Unit = {\n");


        _source.append("    //-------------Constraint--------------\n");
        for (int i = 0; i < _varObservationList.size(); i++)
            _source.append("    val ")
                    .append("Constraint_")
                    .append(i)
                    .append(" = Flip(")
                    .append(_varObservationList.get(i).getValue()).append(")\n");
        _source.append("\n");

        _source.append("    //-------------Semantic--------------\n");
        for (String varFigaroID : _undefinedVarList)
            _source.append("    val ")
                    .append(varFigaroID)
                    .append(" = Flip(")
                    .append(Util.STRATEGY_LOW_PROBABILITY).append(")\n");

        for (DynamicMsg msg : _msgList)
            _source.append(msg.genSource());
        _source.append("\n");

        /*_source.append("    //-------------Constraint--------------\n");
        for (int i = 0; i < _varObservationList.size(); i++) {
            Pair<String, Double> constraint = _varObservationList.get(i);
            _source.append("    val ");
            _source.append("Constraint_");
            _source.append(i);
            _source.append(" = If(");
            _source.append(constraint.getKey());
            _source.append(", Flip(");
            _source.append(constraint.getValue());
            _source.append("), Flip(");
            _source.append(1 - constraint.getValue());
            _source.append("))\n");
        }
        _source.append("\n");*/

        _source.append("    //-------------Observation--------------\n");
        for (DynamicMsg msg : _msgList)
            if (msg instanceof DynamicEntry)
                _source.append("    ")
                        .append(msg.getFigaroID())
                        .append(".observe(true)\n");
        _source.append("\n");

        /*_source.append("    //-------------Constraint--------------\n");
        for (Pair<String, Double> it : _varObservationList) {
            _source.append("    ");
            _source.append(it.getKey());
            _source.append(".addConstraint((b: Boolean) => if (b) ");
            _source.append(it.getValue());
            _source.append(" else ");
            _source.append(1 - it.getValue());
            _source.append(")\n");
        }
        _source.append("\n");*/

        _source.append("    //-------------Sampling--------------\n")
                .append("    val samplePatchValid = VariableElimination(Ret)\n")
                .append("    samplePatchValid.start()\n")
                .append("    println(samplePatchValid.probability(Ret, true))\n")
                .append("    samplePatchValid.kill()\n")
                .append("  }\n")
                .append("}\n");

        return _source.toString();
    }

    private static String genDefinitionSourceStatement(String def, ArrayList<String> useList, double lowProbability) {
        int size = useList.size();
        StringBuilder source = new StringBuilder("    val " + def + " = RichCPD(");
        for (String use : useList)
            source.append(use).append(", ");
        source.append("\n");

        source.append("      (" + "OneOf(true)");
        for (int i = 1; i < size; i++)
            source.append(", OneOf(true)");
        source.append(") -> Flip(" + Util.SEMANTIC_HIGH_PROBABILITY + "),\n");

        source.append("      (" + "*");
        for (int i = 1; i < size; i++)
            source.append(", *");
        source.append(") -> Flip(" + lowProbability +"))\n");

        return source.toString();
    }

    static String genDefinitionSource(String def, ArrayList<String> useList, double lowProbability) {
        int size = useList.size();
        if (size == 0) {
            LevelLogger.warn("WARNING : Empty UseList : def " + def);
            return "    val " + def + " = Flip(" + Util.SEMANTIC_CONSTANT_PROBABILITY + ")\n";
        }

        if (size == 1)
            return "    val " + def + " = If(" + useList.get(0) + ", Flip(" + Util.SEMANTIC_HIGH_PROBABILITY + "), Flip(" + lowProbability +"))\n";

        StringBuilder source = new StringBuilder();
        for (int i = 1; size > 5; i++) {
            ArrayList<String> tmpUseList = new ArrayList<>(useList.subList(0, 5));
            String tmpDef = def + "_tmp" + i;
            source.append(genDefinitionSourceStatement(tmpDef, tmpUseList, lowProbability));
            useList.remove(0);
            useList.remove(0);
            useList.remove(0);
            useList.remove(0);
            useList.remove(0);
            useList.add(0, tmpDef);
            size -= 4;
        }
        source.append(genDefinitionSourceStatement(def, useList, lowProbability));
        return source.toString();
    }
}
