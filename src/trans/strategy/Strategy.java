package trans.strategy;

import javafx.util.Pair;
import trans.dynamicUtils.DynamicMsg;

import java.util.ArrayList;

public class Strategy {

    private static ArrayList<Strategy> _stgList = new ArrayList<>();

    public void init() {
        _stgList.add(new StrategyOdevity());
        _stgList.add(new StrategyPositive());
    }

    public ArrayList<Pair<String, Double>> parse(ArrayList<DynamicMsg> msgList) {
        ArrayList<Pair<String, Double>> observationList = new ArrayList<>();
        for (Strategy stg : _stgList)
            observationList.addAll(stg.parse(msgList));
        return observationList;
    }
}
