package trans.staticUtils;

public class VarNode {
    private String _id;
    private String _name;

    private static String transID2Name(String varID) {
        return varID.replace('.', '_');
    }

    VarNode(String varID) {
        _id = varID;
        _name = transID2Name(varID);
    }

    public String getName() {
        return _name;
    }

    public String getID() {
        return _id;
    }

}
