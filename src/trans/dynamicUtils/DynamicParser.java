package trans.dynamicUtils;

import trans.common.Util;
import trans.staticUtils.StaticInfo;

import org.json.simple.*;

import java.io.File;
import java.util.ArrayList;

public class DynamicParser {

    public static DynamicInfo analyze(File jsonFile, StaticInfo stcInfo) {
        return analyze(jsonFile, stcInfo, new ArrayList<>(), new ArrayList<>());
    }

    public static DynamicInfo analyze(File jsonFile, StaticInfo stcInfo, ArrayList<String> entryList, ArrayList<String> retList) {
        String jsonSource = Util.readFileToString(jsonFile);
        JSONObject rootObject = (JSONObject)JSONValue.parse(jsonSource);
        DynamicInfo dynamicInfo = new DynamicInfo(stcInfo, rootObject, entryList, retList);
        dynamicInfo.parse();
        return dynamicInfo;
    }
}
