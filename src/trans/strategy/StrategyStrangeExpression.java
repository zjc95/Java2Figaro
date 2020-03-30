package trans.strategy;

import javafx.util.Pair;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.InfixExpression;
import trans.common.Util;
import trans.dynamicUtils.DynamicAssign;
import trans.dynamicUtils.DynamicCtrlExpr;
import trans.dynamicUtils.DynamicMsg;

import java.util.ArrayList;

public class StrategyStrangeExpression extends Strategy {

    private static Pair<String, Double> checkCtrlExprNode(DynamicCtrlExpr dynamicCtrlExpr) {
        ASTNode node = dynamicCtrlExpr.getNode();
        if (node instanceof InfixExpression) {
            InfixExpression infixExpression = (InfixExpression) node;
            ASTNode leftOperand = infixExpression.getLeftOperand();
            ASTNode rightOperand = infixExpression.getRightOperand();
            if (leftOperand.toString().equals(rightOperand.toString()))
                return new Pair<>(dynamicCtrlExpr.getFigaroID(), Util.STRATEGY_LOW_PROBABILITY);
        }
        else if (node instanceof BooleanLiteral)
            return new Pair<>(dynamicCtrlExpr.getFigaroID(), Util.STRATEGY_LOW_PROBABILITY);
        return null;
    }

    private static Pair<String, Double> checkAssignNode(DynamicAssign dynamicAssign) {
        ASTNode node = dynamicAssign.getNode();
        if (node instanceof Assignment) {
            Assignment assignment = (Assignment) node;
            ASTNode leftHandSide = assignment.getLeftHandSide();
            ASTNode rightHandSide = assignment.getRightHandSide();
            if (leftHandSide.toString().equals(rightHandSide.toString()))
                return new Pair<>(dynamicAssign.getFigaroID(), Util.STRATEGY_LOW_PROBABILITY);
        }
        return null;
    }

    public ArrayList<Pair<String, Double>> parse(ArrayList<DynamicMsg> msgList) {
        ArrayList<Pair<String, Double>> observationList = new ArrayList<>();

        for (DynamicMsg msg : msgList)
            if (msg instanceof DynamicCtrlExpr) {
                DynamicCtrlExpr dynamicCtrlExpr = (DynamicCtrlExpr) msg;
                Pair<String, Double> ret = checkCtrlExprNode(dynamicCtrlExpr);
                if (ret != null) observationList.add(ret);
            }
            else if (msg instanceof DynamicAssign) {
                DynamicAssign dynamicAssign = (DynamicAssign) msg;
                Pair<String, Double> ret = checkAssignNode(dynamicAssign);
                if (ret != null) observationList.add(ret);
            }

        return observationList;
    }
}
