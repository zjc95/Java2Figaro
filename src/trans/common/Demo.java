package trans.common;

import trans.dynamicUtils.DynamicInfo;
import trans.dynamicUtils.DynamicParser;
import trans.staticUtils.StaticInfo;
import trans.staticUtils.StaticParser;
import trans.trace.TraceParser;

import java.util.ArrayList;

public class Demo {

    public static void main(String[] args) {
        String originProjectPath = System.getProperty("user.dir") + "\\resources\\test\\origin1";
        String srcFilePath = "\\src\\main\\java\\introclassJava\\median_1bf73a9c_000.java";
        String testName = "median_1bf73a9c_000WhiteboxTest#test3";
        //String originProjectPath = System.getProperty("user.dir") + "\\resources\\test\\origin4";
        //String srcFilePath = "\\src\\main\\java\\introclassJava\\checksum_2c155667_003.java";
        //String testName = "checksum_2c155667_003WhiteboxTest#test3";
        String methodName = "exec";

        ArrayList<String> exEntry = new ArrayList<>();
        ArrayList<String> exRet = new ArrayList<>();
        exEntry.add("scanner");
        exEntry.add("output");
        exRet.add("output");

        Double probability = Util.run(originProjectPath, srcFilePath, testName, methodName, exEntry, exRet);
        System.out.println(probability);
    }
}
