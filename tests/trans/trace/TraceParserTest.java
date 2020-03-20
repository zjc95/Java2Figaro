package trans.trace;

import org.junit.*;
import trans.common.Util;

import java.io.File;

public class TraceParserTest {
    @Test
    public void testWithoutIf() {
        File resourceDirectory = new File(System.getProperty("user.dir"), "resources");
        File srcFile = new File(resourceDirectory, "testParser.java");
        File outFile = new File(resourceDirectory, "testParserTrace.java");
        String source = TraceParser.analyze(srcFile, "func");
        Util.write(source, outFile, false);
    }

    @Test
    public void testGenTrace() {
        String className = "testParser";

        File resourceDirectory = new File(System.getProperty("user.dir"), "resources");
        File srcFile = new File(resourceDirectory, className + ".java");
        File outFile = new File(resourceDirectory, className + "Trace.java");
        String source = TraceParser.analyze(srcFile, "func");
        Util.write(source, outFile, false);

        //System.out.println("javac -cp " + Util.JAVA2FIGARO_JAR_PATH + " " + outFile);
        //System.out.println("java -cp " + resourceDirectory.getAbsolutePath() + ";" + Util.JAVA2FIGARO_JAR_PATH + " " + className);
        TraceParser.runCmd("javac -cp " + Util.JAVA2FIGARO_JAR_FILE.getAbsolutePath() + " " + outFile);
        TraceParser.runCmd("java -cp " + resourceDirectory.getAbsolutePath() + ";" + Util.JAVA2FIGARO_JAR_FILE.getAbsolutePath() + " " + className);
    }

    @Test
    public void testWithIf() {
        File resourceDirectory = new File(System.getProperty("user.dir"), "resources");
        File srcFile = new File(resourceDirectory, "testIf.java");
        File outFile = new File(resourceDirectory, "testParserTrace.java");
        String source = TraceParser.analyze(srcFile, "func");
        Util.write(source, outFile, false);
    }

    @Test
    public void testField() {
        File resourceDirectory = new File(System.getProperty("user.dir"), "resources");
        File srcFile = new File(resourceDirectory, "testField.java");
        File outFile = new File(resourceDirectory, "testParserTrace.java");
        String source = TraceParser.analyze(srcFile, "func");
        Util.write(source, outFile, false);
    }
}