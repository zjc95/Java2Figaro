package Trans.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class VarNode {
    private String _name;

    static String transID2Name(String varID) {
        return varID.replace('.', '_');
    }

    VarNode(String varName) {
        _name = varName;
    }

    String getName() {
        return _name;
    }

    /*public String getID() {
        return transName2ID(_name);
    }

    public static String transName2ID(String varName) {
        return varName.replace('_','.');
    }

    private Map<String, VarNode> _dependency = new HashMap<>();

    public boolean setDependency(VarNode dep) {
        String depName = dep.getName();
        if (_dependency.containsKey(depName))
            return false;
        _dependency.put(depName, dep);
        return true;
    }

    public ArrayList<VarNode> getDependency() {
        return new ArrayList<>(_dependency.values());
    }*/
}
