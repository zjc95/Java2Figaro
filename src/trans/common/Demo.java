package trans.common;

import trans.dynamicUtils.DynamicInfo;
import trans.dynamicUtils.DynamicParser;
import trans.staticUtils.StaticInfo;
import trans.staticUtils.StaticParser;

public class Demo {

    public static void main(String[] args) {
        String srcFile = System.getProperty("user.dir") + "\\resources\\test.java";
        String jsonFile = System.getProperty("user.dir") + "\\resources\\test.json";
        String outFile = System.getProperty("user.dir") + "\\resources\\test.scala";
        StaticInfo stcInfo = StaticParser.Analyze(srcFile);
        DynamicInfo dycInfo = DynamicParser.Analyze(jsonFile, stcInfo);
//        print(dycInfo.genFigaroSource(), outFile);
    }




}
