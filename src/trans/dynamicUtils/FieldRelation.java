package trans.dynamicUtils;

import java.util.ArrayList;

public class FieldRelation {
    private String _def;
    private ArrayList<String> _useList;

    FieldRelation(String def, ArrayList<String> useList) {
        _def = def;
        _useList = useList;
    }

    String getDef() {
        return _def;
    }

    ArrayList<String> getUse() {
        return _useList;
    }
}
