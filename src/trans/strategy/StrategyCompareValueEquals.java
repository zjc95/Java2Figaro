package trans.strategy;

import javafx.util.Pair;
import org.eclipse.jdt.core.dom.*;
import trans.common.LevelLogger;
import trans.common.Util;
import trans.dynamicUtils.DynamicAssign;
import trans.dynamicUtils.DynamicCtrlExpr;
import trans.dynamicUtils.DynamicMsg;
import trans.dynamicUtils.DynamicStmt;
import trans.staticUtils.ControlExpression;
import trans.staticUtils.Stmt;

import java.util.*;

public class StrategyCompareValueEquals extends Strategy {
    private Map<String, String> values = new HashMap<>();

    private String getNodeValue(Expression node) {
        if ((node instanceof SuperFieldAccess) || (node instanceof Name) || (node instanceof FieldAccess))
            return values.get(node.toString());
        return null;
    }

    private boolean checkSupplementaryExpression(ControlExpression expr1, ControlExpression expr2) {
        ASTNode node1 = expr1.getNode();
        ASTNode node2 = expr2.getNode();

        if ((!(node1 instanceof InfixExpression)) || (!(node2 instanceof InfixExpression)))
            return false;
        InfixExpression infixExpression1 = (InfixExpression) node1;
        InfixExpression infixExpression2 = (InfixExpression) node2;
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(infixExpression1.getLeftOperand().toString());
        hashSet.add(infixExpression1.getRightOperand().toString());
        if ((!(hashSet.contains(infixExpression2.getLeftOperand().toString())))
                || (!(hashSet.contains(infixExpression2.getRightOperand().toString()))))
            return false;

        String operator = infixExpression2.getOperator().toString();
        return operator.equals("==") || operator.equals("<=") || operator.equals(">=");
    }

    private boolean checkSupplementaryExpression(ControlExpression controlExpression) {
        //System.out.println("check : " + controlExpression.getNode().toString());
        Stmt stmt = controlExpression.getParentStmt();
        for (Stmt structure = stmt.getStructure(); structure != null; structure = structure.getStructure()) {
            for (ControlExpression expr : structure.getControlExprList()) {
                //System.out.println("check to : " + expr.getNode().toString());
                if (checkSupplementaryExpression(controlExpression, expr))
                    return true;
            }
        }

        LinkedList<Stmt> controlStmtList = new LinkedList<>(stmt.getControlStmtList());
        while (!controlStmtList.isEmpty()) {
            Stmt child = controlStmtList.pollFirst();
            for (ControlExpression expr : child.getControlExprList()) {
                //System.out.println("check to : " + expr.getNode().toString());
                if (checkSupplementaryExpression(controlExpression, expr))
                    return true;
            }
        }

        return false;
    }

    private Pair<String, Double> checkEquals(DynamicCtrlExpr dynamicCtrlExpr) {
        ControlExpression controlExpression = (ControlExpression) dynamicCtrlExpr.getMsg();
        ASTNode node = dynamicCtrlExpr.getNode();
        if (node == null)
            return null;
        if (!(node instanceof InfixExpression))
            return null;
        InfixExpression infixExpression = (InfixExpression) node;

        if (!(infixExpression.getOperator().toString().equals("<")) &&
                !(infixExpression.getOperator().toString().equals(">")))
            return null;

        String leftValue = getNodeValue(infixExpression.getLeftOperand());
        String rightValue = getNodeValue(infixExpression.getRightOperand());

        if ((leftValue != null) && (leftValue.equals(rightValue)) && (!checkSupplementaryExpression(controlExpression)))
            return new Pair<>(dynamicCtrlExpr.getFigaroID(), Util.STRATEGY_LOW_PROBABILITY);
        return null;
    }

    public ArrayList<Pair<String, Double>> parse(ArrayList<DynamicMsg> msgList) {
        ArrayList<Pair<String, Double>> observationList = new ArrayList<>();
        values.clear();
        for (DynamicMsg msg : msgList)
            if (msg instanceof DynamicAssign)
                updateValues((DynamicAssign) msg);
            else if (msg instanceof DynamicCtrlExpr) {
                DynamicCtrlExpr dynamicCtrlExpr = (DynamicCtrlExpr) msg;
                Pair<String, Double> ret = checkEquals(dynamicCtrlExpr);
                if (ret != null) observationList.add(ret);
            }
        return observationList;
    }

    private void updateValues(DynamicAssign msg) {
        if (msg.getValue().length() == 0)
            return;

        String defID = msg.getDefVar().getID();
        ASTNode node = msg.getNode();
        String value = msg.getValue();
        if (node == null)
            return;

        if (node instanceof Assignment) {
            Assignment assignment = (Assignment) node;
            if (assignment.getOperator().toString().equals("="))
                values.put(defID, value);
            else
                values.put(defID, getValue(values.get(defID), value, assignment.getOperator().toString()));
        }
        else
            values.put(defID, value);
    }

    private static final int VALUE_TYPE_INT = 0;
    private static final int VALUE_TYPE_DOUBLE = 1;
    private static final int VALUE_TYPE_STRING = 2;

    private String getValue(String value, String update, String sign) {
        if (value == null) return null;
        int valueType = getValueType(value);
        int updateType = getValueType(update);
        if ((valueType == VALUE_TYPE_INT) && (updateType == VALUE_TYPE_INT))
            switch (sign) {
                case "+=":
                    return String.valueOf(Integer.parseInt(value) + Integer.parseInt(update));
                case "-=":
                    return String.valueOf(Integer.parseInt(value) - Integer.parseInt(update));
                case "*=":
                    return String.valueOf(Integer.parseInt(value) * Integer.parseInt(update));
                case "/=":
                    return String.valueOf(Integer.parseInt(value) / Integer.parseInt(update));
                case "&=":
                    return String.valueOf(Integer.parseInt(value) & Integer.parseInt(update));
                case "|=":
                    return String.valueOf(Integer.parseInt(value) | Integer.parseInt(update));
                case "%=":
                    return String.valueOf(Integer.parseInt(value) % Integer.parseInt(update));
                case "^=":
                    return String.valueOf(Integer.parseInt(value) ^ Integer.parseInt(update));
                case "<<=":
                    return String.valueOf(Integer.parseInt(value) << Integer.parseInt(update));
                case ">>=":
                    return String.valueOf(Integer.parseInt(value) >> Integer.parseInt(update));
                default:
                    return String.valueOf(Integer.parseInt(value) >>> Integer.parseInt(update));
            }

        if ((valueType != VALUE_TYPE_STRING) && (updateType != VALUE_TYPE_STRING))
            switch (sign) {
                case "+=":
                    return String.valueOf(Double.parseDouble(value) + Double.parseDouble(update));
                case "-=":
                    return String.valueOf(Double.parseDouble(value) - Double.parseDouble(update));
                case "*=":
                    return String.valueOf(Integer.parseInt(value) * Double.parseDouble(update));
                case "/=":
                    return String.valueOf(Integer.parseInt(value) / Double.parseDouble(update));
                default:
                    return null;
            }

        if (sign.equals("+="))
            return value + update;
        return null;
    }

    private int getValueType(String value) {
        try {
            Integer.parseInt(value);
            return VALUE_TYPE_INT;
        } catch (NumberFormatException ignored) {
        }

        try {
            Double.parseDouble(value);
            return VALUE_TYPE_DOUBLE;
        } catch (NumberFormatException ignored) {
        }

        return VALUE_TYPE_STRING;
    }
}
