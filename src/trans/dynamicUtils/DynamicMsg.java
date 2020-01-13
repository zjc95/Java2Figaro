package trans.dynamicUtils;

import trans.staticUtils.StaticMsg;
import trans.staticUtils.Stmt;
import trans.demo.LevelLogger;

import java.util.ArrayList;

public class DynamicMsg {
    private int _column;
    private int _line;
    protected StaticMsg _msg;
    protected DynamicInfo _info;
    protected String _figaroID = null;

    DynamicMsg(DynamicInfo info, int line, int column, StaticMsg msg) {
        _info = info;
        _line = line;
        _column = column;
        _msg = msg;
    }

    int getLine() {
        return _line;
    }

    int getColumn() {
        return  _column;
    }

    String getKey() {
        return _line + "_" + _column;
    }

    void parse() {}

    String genSource() {
        return null;
    }

    String getFigaroID() { return _figaroID; }


    //----------------Util--------------------

    protected DynamicStmt getStructure(Stmt stmt) {
        Stmt structure = stmt.getStructure();
        if (structure != null) {
            DynamicStmt dycStructure = _info.getStructure(structure);
            if (dycStructure == null)
                LevelLogger.error("ERROR : Structure Not Found : line " + getLine() + " , column " + getColumn());
            return dycStructure;
        }
        return null;
    }

    protected String genDefinitionSource(ArrayList<String> useList) {
        int size = useList.size();
        if (size == 0) {
            LevelLogger.warn("WARNING: Empty UseList : line " + getLine() + " , column " + getColumn());
            return "    val " + _figaroID + " = Flip(0.5)\n";
        }

        if (size == 1)
            return "    val " + _figaroID + " = If(" + useList.get(0) + ", Flip(0.95), Flip(0.05))\n";

        StringBuilder source = new StringBuilder("    val " + _figaroID + " = RichCPD(");
        for (String use : useList)
            source.append(use).append(", ");
        source.append("\n");

        source.append("      (" + "OneOf(true)");
        for (int i = 1; i < size; i++)
            source.append(", OneOf(true)");
        source.append(") -> Flip(0.95),\n");

        source.append("      (" + "*");
        for (int i = 1; i < size; i++)
            source.append(", *");
        source.append(") -> Flip(0.05))\n");

        return source.toString();
    }
}
