package Trans.demo;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Stmt {
    private int _line = 0;
    private SourceInform _inform;
    private ASTNode _node;
    private ArrayList<Assign> _assign = new ArrayList<>();
    private Map<String, VarNode> _use = new HashMap<>();

    Stmt(SourceInform inform, ASTNode node, int line){
        _inform = inform;
        _node = node;
        _line = line;
    }

    ASTNode getNode() {
        return _node;
    }

    void addAssign(Assign assign) {
        _assign.add(assign);
    }

    ArrayList<Assign> getAssign() {
        return _assign;
    }

    void addUse(VarNode use) {
        String varName = use.getName();
        if (!_use.containsKey(varName))
            _use.put(varName, use);
    }

    ArrayList<VarNode> getUse() {
        return new ArrayList<>(_use.values());
    }

    int getLine() {
        return _line;
    }

}
