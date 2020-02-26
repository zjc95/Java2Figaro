package trans.staticUtils;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;

public class ControlExpression extends StaticMsg {
    private ArrayList<VarNode> _useList = new ArrayList<>();

    ControlExpression(int line, int column, ASTNode node) {
        super(line, column, node);
    }

    void addUse(VarNode var) {
        _useList.add(var);
    }

    public ArrayList<VarNode> getUseList() {
        return _useList;
    }
}
