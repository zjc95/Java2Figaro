package trans.dynamicUtils;

import trans.common.Util;
import trans.staticUtils.StaticInfo;
import trans.common.LevelLogger;

import org.json.simple.*;

import java.util.ArrayList;

public class DynamicParser {

    public static DynamicInfo Analyze(String jsonFile, StaticInfo stcInfo) {
        return Analyze(jsonFile, stcInfo, new ArrayList<>(), new ArrayList<>());
    }

    public static DynamicInfo Analyze(String jsonFile, StaticInfo stcInfo, ArrayList<String> entryList, ArrayList<String> retList) {
        String jsonSource = Util.readFileToString(jsonFile);
        JSONObject rootObject = (JSONObject)JSONValue.parse(jsonSource);
        DynamicInfo dycInfo = new DynamicInfo(stcInfo, rootObject, entryList, retList);
        dycInfo.parse();
        return dycInfo;
    }
}
