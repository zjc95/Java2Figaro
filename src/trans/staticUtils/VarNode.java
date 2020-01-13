package trans.staticUtils;

public class VarNode {
    private String _name;

    static String transID2Name(String varID) {
        return varID.replace('.', '_');
    }

    private static String transName2ID(String varName) {
        return varName.replace('_','.');
    }

    VarNode(String varName) {
        _name = varName;
    }

    public String getName() {
        return _name;
    }

    public String getID() {
        return transName2ID(_name);
    }

}
