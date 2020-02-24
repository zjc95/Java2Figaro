package trans.staticUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VarNode {
    private String _id;
    private String _name;
    private Map<String, VarNode> _subjectList = new HashMap<>();
    private Map<String, VarNode> _fieldList = new HashMap<>();

    private static String transID2Name(String varID) {
        return varID.replace('.', '_');
    }

    public VarNode(String varID) {
        _id = varID;
        _name = transID2Name(varID);
    }

    public String getName() {
        return _name;
    }

    public String getID() {
        return _id;
    }

    public boolean checkField(String subjectID) {
        return _fieldList.containsKey(subjectID);
    }

    public ArrayList<VarNode> getField() {
        return new ArrayList<>(_fieldList.values());
    }

    public ArrayList<VarNode> getSubject() {
        return new ArrayList<>(_subjectList.values());
    }

    void addSubject(VarNode subject) {
        _subjectList.put(subject.getID(), subject);
    }

    void addField(VarNode field) {
        _fieldList.put(field.getID(), field);
    }
}
