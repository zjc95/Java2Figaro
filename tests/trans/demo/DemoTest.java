package trans.demo;

import trans.dynamicUtils.DynamicInfo;
import trans.dynamicUtils.DynamicParser;
import trans.staticUtils.StaticInfo;
import trans.staticUtils.StaticParser;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class DemoTest {

    @Test
    public void testWithoutIf() {
        String srcFile = System.getProperty("user.dir") + "\\resources\\test.java";
        String jsonFile = System.getProperty("user.dir") + "\\resources\\test.json";
        String outFile = System.getProperty("user.dir") + "\\resources\\test.scala";
        StaticInfo stcInfo = StaticParser.Analyze(srcFile);
        DynamicInfo dycInfo = DynamicParser.Analyze(jsonFile, stcInfo);
        print(dycInfo.genFigaroSource(), outFile);
    }

    @Test
    public void testWithIf1() {
        String srcFile = System.getProperty("user.dir") + "\\resources\\testIF.java";
        String jsonFile = System.getProperty("user.dir") + "\\resources\\testIF1.json";
        String outFile = System.getProperty("user.dir") + "\\resources\\testIF1.scala";
        StaticInfo stcInfo = StaticParser.Analyze(srcFile);
        DynamicInfo dycInfo = DynamicParser.Analyze(jsonFile, stcInfo);
        print(dycInfo.genFigaroSource(), outFile);
    }

    @Test
    public void testWithIf2() {
        String srcFile = System.getProperty("user.dir") + "\\resources\\testIF.java";
        String jsonFile = System.getProperty("user.dir") + "\\resources\\testIF2.json";
        String outFile = System.getProperty("user.dir") + "\\resources\\testIF2.scala";
        StaticInfo stcInfo = StaticParser.Analyze(srcFile);
        DynamicInfo dycInfo = DynamicParser.Analyze(jsonFile, stcInfo);
        print(dycInfo.genFigaroSource(), outFile);
    }

    @Test
    public void testWithIf3() {
        String srcFile = System.getProperty("user.dir") + "\\resources\\testIF.java";
        String jsonFile = System.getProperty("user.dir") + "\\resources\\testIF3.json";
        String outFile = System.getProperty("user.dir") + "\\resources\\testIF3.scala";
        StaticInfo stcInfo = StaticParser.Analyze(srcFile);
        DynamicInfo dycInfo = DynamicParser.Analyze(jsonFile, stcInfo);
        print(dycInfo.genFigaroSource(), outFile);
    }

    private static void print(String str, String outFile) {
        File file = new File(outFile);
        try (PrintWriter output = new PrintWriter(file)) {
            output.print(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}