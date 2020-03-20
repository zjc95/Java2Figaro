package trans.common;

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
        File resourceDirectory = new File(System.getProperty("user.dir"), "resources");
        File srcFile = new File(resourceDirectory, "test.java");
        File jsonFile = new File(resourceDirectory, "test.json");
        File outFile = new File(resourceDirectory, "test.scala");
        StaticInfo stcInfo = StaticParser.analyze(srcFile);
        DynamicInfo dycInfo = DynamicParser.analyze(jsonFile, stcInfo);
        Util.write(dycInfo.genFigaroSource(), outFile, false);
    }

    @Test
    public void testWithIf1() {
        File resourceDirectory = new File(System.getProperty("user.dir"), "resources");
        File srcFile = new File(resourceDirectory, "testIF.java");
        File jsonFile = new File(resourceDirectory, "testIF1.json");
        File outFile = new File(resourceDirectory, "testIF1.scala");
        StaticInfo stcInfo = StaticParser.analyze(srcFile);
        DynamicInfo dycInfo = DynamicParser.analyze(jsonFile, stcInfo);
        Util.write(dycInfo.genFigaroSource(), outFile, false);
    }

    @Test
    public void testWithIf2() {
        File resourceDirectory = new File(System.getProperty("user.dir"), "resources");
        File srcFile = new File(resourceDirectory, "testIF.java");
        File jsonFile = new File(resourceDirectory, "testIF2.json");
        File outFile = new File(resourceDirectory, "testIF2.scala");
        StaticInfo stcInfo = StaticParser.analyze(srcFile);
        DynamicInfo dycInfo = DynamicParser.analyze(jsonFile, stcInfo);
        Util.write(dycInfo.genFigaroSource(), outFile, false);
    }

    @Test
    public void testWithIf3() {
        File resourceDirectory = new File(System.getProperty("user.dir"), "resources");
        File srcFile = new File(resourceDirectory, "testIF.java");
        File jsonFile = new File(resourceDirectory, "testIF3.json");
        File outFile = new File(resourceDirectory, "testIF3.scala");
        StaticInfo stcInfo = StaticParser.analyze(srcFile);
        DynamicInfo dycInfo = DynamicParser.analyze(jsonFile, stcInfo);
        Util.write(dycInfo.genFigaroSource(), outFile, false);
    }

    @Test
    public void testWithField() {
        File resourceDirectory = new File(System.getProperty("user.dir"), "resources");
        File srcFile = new File(resourceDirectory, "testField.java");
        File jsonFile = new File(resourceDirectory, "testField.json");
        File outFile = new File(resourceDirectory, "testField.scala");
        StaticInfo stcInfo = StaticParser.analyze(srcFile);
        DynamicInfo dycInfo = DynamicParser.analyze(jsonFile, stcInfo);
        Util.write(dycInfo.genFigaroSource(), outFile, false);
    }
}