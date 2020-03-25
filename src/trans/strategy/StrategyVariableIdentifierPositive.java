package trans.strategy;

import javafx.util.Pair;
import trans.common.Util;
import trans.dynamicUtils.DynamicAssign;
import trans.dynamicUtils.DynamicMsg;

import java.util.ArrayList;

public class StrategyVariableIdentifierPositive extends Strategy{
    private Pair<String, Double> checkPositive(String def, String figaroID, String str) {
        try {
            int val = Integer.parseInt(str);

            if (def.endsWith("size") || def.endsWith("Size"))
                if (val > 0) return new Pair<>(figaroID, Util.STRATEGY_HIGH_PROBABILITY);
                else return new Pair<>(figaroID, Util.STRATEGY_LOW_PROBABILITY);

            if (def.endsWith("len") || def.endsWith("Len"))
                if (val > 0) return new Pair<>(figaroID, Util.STRATEGY_HIGH_PROBABILITY);
                else return new Pair<>(figaroID, Util.STRATEGY_LOW_PROBABILITY);

            if (def.endsWith("length") || def.endsWith("Length"))
                if (val > 0) return new Pair<>(figaroID, Util.STRATEGY_HIGH_PROBABILITY);
                else return new Pair<>(figaroID, Util.STRATEGY_LOW_PROBABILITY);

            return null;
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public ArrayList<Pair<String, Double>> parse(ArrayList<DynamicMsg> msgList) {
        ArrayList<Pair<String, Double>> observationList = new ArrayList<>();
        for (DynamicMsg msg : msgList)
            if (msg instanceof DynamicAssign)
            {
                DynamicAssign ass = (DynamicAssign) msg;
                Pair<String, Double> ret = checkPositive(ass.getDefVar().getID(), ass.getFigaroID(), ass.getValue());
                if (ret != null) observationList.add(ret);
            }
        return observationList;
    }
}
