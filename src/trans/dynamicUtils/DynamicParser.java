package trans.dynamicUtils;

import trans.staticUtils.StaticInfo;
import trans.demo.LevelLogger;

import org.json.simple.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DynamicParser {

    public static DynamicInfo Analyze(String jsonFile, StaticInfo stcInfo) {
        String jsonSource = readFileToString(jsonFile);
        JSONObject rootObject = (JSONObject)JSONValue.parse(jsonSource);

        String srcFile = (String) rootObject.get("file");
        LevelLogger.debug("Source File : " + srcFile);

        DynamicInfo dycInfo = new DynamicInfo(stcInfo, rootObject);
        dycInfo.parse();
        return dycInfo;
    }

    private static String readFileToString(String jsonFile) {
        if (jsonFile == null) {
            LevelLogger.error("#readFileToString Illegal input file path : null.");
            return "";
        }

        File file = new File(jsonFile);
        if (!file.exists() || !file.isFile()) {
            LevelLogger.error("#readFileToString Illegal input file path : " + jsonFile);
            return "";
        }

        StringBuffer stringBuffer = new StringBuffer();
        InputStream in = null;
        InputStreamReader inputStreamReader = null;
        try {
            in = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            char[] ch = new char[1024];
            int readCount = 0;
            while ((readCount = inputStreamReader.read(ch)) != -1) {
                stringBuffer.append(ch, 0, readCount);
            }
            inputStreamReader.close();
            in.close();

        } catch (Exception e) {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e1) {
                    return "";
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    return "";
                }
            }
        }
        return stringBuffer.toString();
    }
}
