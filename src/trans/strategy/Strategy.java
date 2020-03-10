package trans.strategy;

import javafx.util.Pair;
import trans.dynamicUtils.DynamicMsg;

import java.util.ArrayList;

public class Strategy {

    private static ArrayList<Strategy> _stgList = new ArrayList<>();

    public void init() {
        _stgList.clear();
        _stgList.add(new StrategyVariableIdentifierOdevity());
        _stgList.add(new StrategyVariableIdentifierPositive());
        _stgList.add(new StrategyCompareValueEquals());
        _stgList.add(new StrategyEntryVariableUse());
        _stgList.add(new StrategyMultipleAssign());
        _stgList.add(new StrategyStrangeExpression());
    }

    public ArrayList<Pair<String, Double>> parse(ArrayList<DynamicMsg> msgList) {
        ArrayList<Pair<String, Double>> observationList = new ArrayList<>();
        for (Strategy stg : _stgList)
            observationList.addAll(stg.parse(msgList));
        return observationList;
    }
}
