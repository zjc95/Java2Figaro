package trans.strategy;

import javafx.util.Pair;
import org.eclipse.jdt.core.dom.*;
import trans.common.Util;
import trans.dynamicUtils.DynamicAssign;
import trans.dynamicUtils.DynamicMsg;
import trans.staticUtils.Assign;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashSet;

public class StrategyMultipleAssign extends Strategy {

    private boolean checkUse(DynamicAssign dynamicAssign, String varID) {
        Assign assign = (Assign) dynamicAssign.getMsg();
        for (VarNode varNode : assign.getUseList())
            if (varNode.getID().equals(varID))
                return true;
        return false;
    }

    private boolean checkNode(DynamicAssign dynamicAssign) {
        ASTNode node = dynamicAssign.getNode();
        while (node != null) {
            if ((node instanceof ForStatement) ||
                    (node instanceof EnhancedForStatement) ||
                    (node instanceof WhileStatement) ||
                    (node instanceof DoStatement))
                return true;
            else
                node = node.getParent();
        }
        return false;
    }

    public ArrayList<Pair<String, Double>> parse(ArrayList<DynamicMsg> msgList) {
        ArrayList<Pair<String, Double>> observationList = new ArrayList<>();
        HashSet<String> varAssignSet = new HashSet<>();
        HashSet<String> varMultiAssignSet = new HashSet<>();

        for (DynamicMsg msg : msgList)
            if (msg instanceof DynamicAssign) {
                DynamicAssign dynamicAssign = (DynamicAssign) msg;
                String varID = dynamicAssign.getDefVar().getID();
                if ((varAssignSet.contains(varID)) && (!varMultiAssignSet.contains(varID)) && (!checkNode(dynamicAssign)) && (!checkUse(dynamicAssign, varID))) {
                    //LevelLogger.debug("MultipleAssign : " + msg.getFigaroID());
                    varMultiAssignSet.add(varID);
                    observationList.add(new Pair<>(msg.getFigaroID(), 0.8));
                }
                varAssignSet.add(varID);
            }

        return observationList;
    }
}
