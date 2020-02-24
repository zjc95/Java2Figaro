package trans.common;

import trans.dynamicUtils.DynamicInfo;
import trans.dynamicUtils.DynamicParser;
import trans.staticUtils.StaticInfo;
import trans.staticUtils.StaticParser;
import trans.trace.TraceParser;

import java.util.ArrayList;

public class Demo {

    public static void main(String[] args) {
        ArrayList<String> exEntry = new ArrayList<>();
        ArrayList<String> exRet = new ArrayList<>();

        String originProjectPath = System.getProperty("user.dir") + "\\resources\\test\\origin";
        String srcFilePath = "\\src\\main\\java\\introclassJava\\median_1bf73a9c_000.java";
        String testName = "median_1bf73a9c_000WhiteboxTest#test3";
        String methodName = "exec";
        exEntry.add("scanner");
        exRet.add("output");

        String workPath = System.getProperty("user.dir") + "\\resources\\test";
        String copyProjectPath = workPath + "\\copy";
        String figaroFile = workPath + "\\patch.scala";
        String jsonFile = workPath + "\\DumpResult.json";
        String dumpJARPath = System.getProperty("user.dir") + "\\lib\\Java2Figaro.jar";
        String figaroJARPath = System.getProperty("user.dir") + "\\lib\\figaro_2.12-5.0.0.0.jar";

        Util.copyProject(originProjectPath, copyProjectPath);
        StaticInfo stcInfo = StaticParser.Analyze(originProjectPath + srcFilePath, methodName);
        String traceCode = TraceParser.Analyze(originProjectPath + srcFilePath, methodName);
        Util.print(traceCode, copyProjectPath + srcFilePath);
        Util.addDependencyToPom(copyProjectPath, dumpJARPath);
        Util.runTest(copyProjectPath, testName);
        DynamicInfo dycInfo = DynamicParser.Analyze(jsonFile, stcInfo, exEntry, exRet);
        Util.print(dycInfo.genFigaroSource(), figaroFile);
        Util.runFigaroProgram(workPath, figaroJARPath);


        /*String srcFile = System.getProperty("user.dir") + "\\resources\\origin\\src\\main\\java\\introclassJava\\median_1bf73a9c_000.java";
        String traceSrcFile = System.getProperty("user.dir") + "\\resources\\copy\\src\\main\\java\\introclassJava\\median_1bf73a9c_000.java";
        String jsonFile = System.getProperty("user.dir") + "\\resources\\DumpResult.json";
        String figaroFile = System.getProperty("user.dir") + "\\resources\\median_1bf73a9c_000.scala";
        String figaroJARPath = "D:\\program\\workspace\\Java2Figaro\\lib\\figaro_2.12-5.0.0.0.jar";
        String methodName = "exec";
        ArrayList<String> exEntry = new ArrayList<>();
        ArrayList<String> exRet = new ArrayList<>();
        exEntry.add("scanner");
        exRet.add("output");
        StaticInfo stcInfo = StaticParser.Analyze(srcFile, methodName);
        String traceCode = TraceParser.Analyze(srcFile, methodName);
        //Util.print(traceCode, traceSrcFile);
        DynamicInfo dycInfo = DynamicParser.Analyze(jsonFile, stcInfo, exEntry, exRet);
        Util.print(dycInfo.genFigaroSource(), figaroFile);*/
    }
}
