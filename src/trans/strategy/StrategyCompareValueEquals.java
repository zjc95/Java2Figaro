package trans.strategy;

import javafx.util.Pair;
import org.eclipse.jdt.core.dom.*;
import trans.dynamicUtils.DynamicAssign;
import trans.dynamicUtils.DynamicCtrlExpr;
import trans.dynamicUtils.DynamicMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StrategyCompareValueEquals extends Strategy {
    private Map<String, String> values = new HashMap<>();

    private String getNodeValue(Expression node) {
        if ((node instanceof SuperFieldAccess) || (node instanceof Name) || (node instanceof FieldAccess))
            return values.get(node.toString());
        return null;
    }

    private Pair<String, Double> checkEquals(DynamicCtrlExpr msg) {
        ASTNode node = msg.getNode();
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

        if ((leftValue != null) && (leftValue.equals(rightValue)))
            return new Pair<>(msg.getFigaroID(), 0.4);
        return null;
    }

    public ArrayList<Pair<String, Double>> parse(ArrayList<DynamicMsg> msgList) {
        ArrayList<Pair<String, Double>> observationList = new ArrayList<>();
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
