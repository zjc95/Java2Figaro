package trans.common;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import trans.dynamicUtils.DynamicInfo;
import trans.dynamicUtils.DynamicParser;
import trans.staticUtils.StaticInfo;
import trans.staticUtils.StaticParser;
import trans.trace.TraceParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class Util {
    public static final int JAVA_LEVEL = AST.JLS8;
    public static final String JAVA_VERSION = JavaCore.VERSION_1_8;
    public static final String LIBRARY_PATH = System.getProperty("user.dir") + "\\lib";
    public static final String FIGARO_JAR_PATH = LIBRARY_PATH + "\\figaro_2.12-5.0.0.0.jar";
    public static final String JAVA2FIGARO_JAR_PATH = LIBRARY_PATH + "\\Java2Figaro.jar";
    public static final String WORK_PATH = System.getProperty("user.dir") + "\\resources\\test";
    public static final String COPY_PROJECT_PATH = WORK_PATH + "\\copy";
    public static final String FIGARO_FILE_PATH = WORK_PATH + "\\patch.scala";
    public static final String JSON_FILE_PATH = COPY_PROJECT_PATH + "\\DumpResult.json";

    public static double run(String originProjectPath, String srcFilePath, String testName, String methodName,
                            ArrayList<String> exEntry, ArrayList<String> exRet) {
        copyProject(originProjectPath);
        StaticInfo stcInfo = StaticParser.Analyze(originProjectPath + srcFilePath, methodName);
        String traceCode = TraceParser.Analyze(originProjectPath + srcFilePath, methodName);
        print(traceCode, COPY_PROJECT_PATH + srcFilePath);
        addDependencyToPom(COPY_PROJECT_PATH);
        runTest(COPY_PROJECT_PATH, testName);
        DynamicInfo dycInfo = DynamicParser.Analyze(JSON_FILE_PATH, stcInfo, exEntry, exRet);
        print(dycInfo.genFigaroSource(), FIGARO_FILE_PATH);
        return runFigaroProgram(WORK_PATH);
    }

    private static void copyProject(String projectPath) {
        String command = "xcopy " + projectPath + " " + COPY_PROJECT_PATH + " /E /I /H /C /Y";
        try {
            Runtime.getRuntime().exec(command).waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void runTest(String projectPath, String testName) {
        String command = "D: && cd " + projectPath + "&& mvn test -Dtest=" + testName;
        LevelLogger.debug("RUN TEST : " + command);
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + command);
            InputStream fis = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while((line = br.readLine()) != null)
                LevelLogger.debug(line);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static double runFigaroProgram(String workPath) {
        double retValue = 0;
        String sourcePath = workPath + "\\patch.scala";
        String command1 = "scalac -cp " + FIGARO_JAR_PATH + " -d " + workPath + " " + sourcePath;
        String command2 = "scala -cp " + workPath + ";" + FIGARO_JAR_PATH + " patch";
        LevelLogger.debug("COMPILE FIGARO SOURCE : " + command1);
        LevelLogger.debug("RUN FIGARO SOURCE : " + command2);

        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + command1 + " && " + command2);
            InputStream fis = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while((line = br.readLine()) != null)
                try {
                    retValue = Double.parseDouble(line);
                    break;
                } catch (NumberFormatException ignored) {
                }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    private static void addDependencyToPom(String projectDir) {
        try {
            File pomFile = new File(projectDir + "\\pom.xml");
            Document document = new SAXReader().read(pomFile);
            Element rootNode = document.getRootElement();
            Element dependenciesNode = rootNode.element("dependencies");
            Element dependencyNode = dependenciesNode.addElement("dependency");

            Element groupIdNode = dependencyNode.addElement("groupId");
            Element artifactIdNode = dependencyNode.addElement("artifactId");
            Element versionNode = dependencyNode.addElement("version");
            Element scopeNode = dependencyNode.addElement("scope");
            Element systemPathNode = dependencyNode.addElement("systemPath");
            groupIdNode.setText("com.java.zjc");
            artifactIdNode.setText("java2figaro");
            versionNode.setText("1.0");
            scopeNode.setText("system");
            systemPathNode.setText(JAVA2FIGARO_JAR_PATH);

            Writer osWrite = new OutputStreamWriter(new FileOutputStream(pomFile));
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(osWrite, format);
            writer.write(document);
            writer.flush();
            writer.close();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }

    public static CompilationUnit genASTFromSource(String srcFile, String srcPath) {
        String source = readFileToString(srcFile);
        if(source.isEmpty()) return null;

        ASTParser astParser = ASTParser.newParser(JAVA_LEVEL);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JAVA_VERSION, options);
        astParser.setCompilerOptions(options);

        astParser.setSource(source.toCharArray());

        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setResolveBindings(true);
        srcPath = srcPath == null ? "" : srcPath;
        astParser.setEnvironment(getClassPath(), new String[] {srcPath}, null, true);
        astParser.setUnitName(srcFile);
        astParser.setBindingsRecovery(true);

        try{
            return (CompilationUnit) astParser.createAST(null);
        }catch(Exception e) {
            return null;
        }
    }

    private static String[] getClassPath() {
        String property = System.getProperty("java.class.path", ".");
        return property.split(File.pathSeparator);
    }

    public static String readFileToString(String srcFile) {
        if (srcFile == null) {
            LevelLogger.error("#readFileToString Illegal input file path : null.");
            return "";
        }

        File file = new File(srcFile);
        if (!file.exists() || !file.isFile()) {
            LevelLogger.error("#readFileToString Illegal input file path : " + srcFile);
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

    public static void print(String str, String outFile) {
        File file = new File(outFile);
        try (PrintWriter output = new PrintWriter(file)) {
            output.print(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
