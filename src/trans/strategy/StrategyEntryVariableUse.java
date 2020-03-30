package trans.strategy;

import javafx.util.Pair;
import trans.common.Util;
import trans.dynamicUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class StrategyEntryVariableUse extends Strategy {

    private static ArrayList<String> getUseList(DynamicMsg msg) {
        if (msg instanceof DynamicAssign) {
            DynamicAssign dynamicAssign = (DynamicAssign) msg;
            return dynamicAssign.getUseList();
        }

        if (msg instanceof DynamicCtrlExpr) {
            DynamicCtrlExpr dynamicCtrlExpr = (DynamicCtrlExpr) msg;
            return dynamicCtrlExpr.getUseList();
        }

        if (msg instanceof DynamicStmt) {
            DynamicStmt dynamicStmt = (DynamicStmt) msg;
            return dynamicStmt.getUseList();
        }

        if (msg instanceof DynamicRet) {
            DynamicRet dynamicRet = (DynamicRet) msg;
            return dynamicRet.getUseList();
        }

        return null;
    }

    public ArrayList<Pair<String, Double>> parse(ArrayList<DynamicMsg> msgList) {
        ArrayList<Pair<String, Double>> observationList = new ArrayList<>();
        Map<String, HashSet<String>> entryMap = new HashMap<>();
        ArrayList<String> entryList = new ArrayList<>();
        ArrayList<String> retUseList = new ArrayList<>();

        for (DynamicMsg msg : msgList) {
            String figaroID = msg.getFigaroID();
            HashSet<String> entryUseSet = new HashSet<>();
            if (msg instanceof DynamicEntry) {
                entryList.add(figaroID);
                entryUseSet.add(figaroID);
                entryMap.put(figaroID, entryUseSet);
                continue;
            }

            if (msg instanceof DynamicStmtEnd) {
                ArrayList<Pair<String, String>> scopeDefList = ((DynamicStmtEnd) msg).getScopeDefList();
                for (Pair<String, String> scopeDef : scopeDefList) {
                    HashSet<String> useVarEntryList = entryMap.get(scopeDef.getValue());
                    if (useVarEntryList == null) continue;
                    useVarEntryList.addAll(entryMap.get(figaroID));
                    entryMap.put(scopeDef.getKey(), useVarEntryList);
                }
                continue;
            }

            ArrayList<String> useList = getUseList(msg);
            if ((useList.size() == 1) && (useList.get(0).equals("Var_scanner"))) {
                entryList.add(figaroID);
                entryUseSet.add(figaroID);
            }

            for (String useVar : useList) {
                HashSet<String> useVarEntryList = entryMap.get(useVar);
                if (useVarEntryList == null) continue;
                entryUseSet.addAll(useVarEntryList);
            }
            entryMap.put(figaroID, entryUseSet);
            //System.out.println(figaroID + ":" + entryUseSet);
            if (figaroID.equals("Ret"))
                retUseList.addAll(useList);
        }

        HashSet<String> retEntryUseSet = entryMap.get("Ret");
        if (retEntryUseSet.size() < entryList.size())
            for (String retUse : retUseList)
                observationList.add(new Pair<>(retUse, Util.STRATEGY_LOW_PROBABILITY));
        return observationList;
    }
}
