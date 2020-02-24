package trans.dynamicUtils;

import trans.common.Util;
import trans.staticUtils.StaticInfo;
import trans.common.LevelLogger;

import org.json.simple.*;

import java.util.ArrayList;

public class DynamicParser {

    public static DynamicInfo Analyze(String jsonFile, StaticInfo stcInfo) {
        String jsonSource = Util.readFileToString(jsonFile);
        JSONObject rootObject = (JSONObject)JSONValue.parse(jsonSource);

        String srcFile = (String) rootObject.get("file");
        LevelLogger.debug("Source File : " + srcFile);

        DynamicInfo dycInfo = new DynamicInfo(stcInfo, rootObject);
        dycInfo.parse();
        return dycInfo;
    }

    public static DynamicInfo Analyze(String jsonFile, StaticInfo stcInfo, ArrayList<String> entrys, ArrayList<String> rets) {
        String jsonSource = Util.readFileToString(jsonFile);
        JSONObject rootObject = (JSONObject)JSONValue.parse(jsonSource);

        String srcFile = (String) rootObject.get("file");
        LevelLogger.debug("Source File : " + srcFile);

        DynamicInfo dycInfo = new DynamicInfo(stcInfo, rootObject, entrys, rets);
        dycInfo.parse();
        return dycInfo;
    }
}
