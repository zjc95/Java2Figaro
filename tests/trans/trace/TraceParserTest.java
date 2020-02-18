package trans.trace;

import org.junit.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class TraceParserTest {
    @Test
    public void testWithoutIf() {
        String srcFile = System.getProperty("user.dir") + "\\resources\\testParser.java";
        String outFile = System.getProperty("user.dir") + "\\resources\\testParserTrace.java";
        String source = TraceParser.Analyze(srcFile);
        print(source, outFile);
    }

    @Test
    public void testWithIf() {
        String srcFile = System.getProperty("user.dir") + "\\resources\\testIf.java";
        String outFile = System.getProperty("user.dir") + "\\resources\\testParserTrace.java";
        String source = TraceParser.Analyze(srcFile);
        print(source, outFile);
    }

    @Test
    public void testField() {
        String srcFile = System.getProperty("user.dir") + "\\resources\\testField.java";
        String outFile = System.getProperty("user.dir") + "\\resources\\testParserTrace.java";
        String source = TraceParser.Analyze(srcFile);
        print(source, outFile);
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