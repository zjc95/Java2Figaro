package trans.strategy;

import javafx.util.Pair;
import trans.common.Util;
import trans.dynamicUtils.DynamicAssign;
import trans.dynamicUtils.DynamicMsg;

import java.util.ArrayList;

public class StrategyVariableIdentifierOdevity extends Strategy{

    private Pair<String, Double> checkOdevity(String def, String figaroID, String string) {
        try {
            int val = Integer.parseInt(string);

            if (def.endsWith("odd") || def.endsWith("Odd"))
                if (val % 2 == 0) return new Pair<>(figaroID, Util.STRATEGY_LOW_PROBABILITY);
                else return new Pair<>(figaroID, Util.STRATEGY_HIGH_PROBABILITY);

            if (def.endsWith("even") || def.endsWith("Even"))
                if (val % 2 == 0) return new Pair<>(figaroID, Util.STRATEGY_HIGH_PROBABILITY);
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
                Pair<String, Double> ret = checkOdevity(ass.getDefVar().getID(), ass.getFigaroID(), ass.getValue());
                if (ret != null) observationList.add(ret);
            }
        return observationList;
    }
}
