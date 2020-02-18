package trans.trace;

import org.junit.*;
import trans.common.Util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class TraceParserTest {
    @Test
    public void testWithoutIf() {
        String srcFile = System.getProperty("user.dir") + "\\resources\\testParser.java";
        String outFile = System.getProperty("user.dir") + "\\resources\\testParserTrace.java";
        String source = TraceParser.Analyze(srcFile);
        Util.print(source, outFile);
    }

    @Test
    public void testGenTrace() {
        String className = "testParser";

        String resourceDir = System.getProperty("user.dir") + "\\resources";
        String srcFile = resourceDir + "\\" + className + ".java";
        String outFile = resourceDir + "\\" + className + "Trace.java";
        String source = TraceParser.Analyze(srcFile);
        Util.print(source, outFile);

        //System.out.println("javac -cp " + Util.JAVA2FIGARO_JAR_PATH + " " + outFile);
        //System.out.println("java -cp " + resourceDir + ";" + Util.JAVA2FIGARO_JAR_PATH + " " + className);
        TraceParser.runCmd("javac -cp " + Util.JAVA2FIGARO_JAR_PATH + " " + outFile);
        TraceParser.runCmd("java -cp " + resourceDir + ";" + Util.JAVA2FIGARO_JAR_PATH + " " + className);
    }

    @Test
    public void testWithIf() {
        String srcFile = System.getProperty("user.dir") + "\\resources\\testIf.java";
        String outFile = System.getProperty("user.dir") + "\\resources\\testParserTrace.java";
        String source = TraceParser.Analyze(srcFile);
        Util.print(source, outFile);
    }

    @Test
    public void testField() {
        String srcFile = System.getProperty("user.dir") + "\\resources\\testField.java";
        String outFile = System.getProperty("user.dir") + "\\resources\\testParserTrace.java";
        String source = TraceParser.Analyze(srcFile);
        Util.print(source, outFile);
    }
}