package trans.common;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import trans.trace.TraceParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Util {
    public static final int JAVA_LEVEL = AST.JLS8;
    public static final String JAVA_VERSION = JavaCore.VERSION_1_8;
    public static final String JAVA2FIGARO_JAR_PATH = System.getProperty("user.dir") + "\\out\\artifacts\\Java2Figaro_jar\\Java2Figaro.jar";

    public static void copyProject(String projectPath, String targetDir) {
        String command = "xcopy " + projectPath + " " + targetDir + " /E /I /H /C /Y";
        try {
            Runtime.getRuntime().exec(command).waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void runTest(String projectPath, String testName) {
        String command = "D: && cd " + projectPath + "&& mvn test -Dtest=" + testName;
        System.out.println(command);
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + command);
            InputStream fis = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while((line = br.readLine()) != null)
                System.out.println(line);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void runFigaroProgram(String workPath, String figaroJARPath) {
        String classesPath = workPath + "\\classes";
        String sourcePath = workPath + "\\patch.scala";
        String command1 = "scalac -cp " + figaroJARPath + " -d " + classesPath + " " + sourcePath;
        String command2 = "scala -cp " + classesPath + ";" + figaroJARPath + " patch";
        System.out.println(command1);
        System.out.println(command2);

        String className = "testParser";
        String resourceDir = System.getProperty("user.dir") + "\\resources";
        String outFile = resourceDir + "\\" + className + "Trace.java";
        String command3 = "javac -cp " + Util.JAVA2FIGARO_JAR_PATH + " " + outFile;
        String command4 = "java -cp " + resourceDir + ";" + Util.JAVA2FIGARO_JAR_PATH + " " + className;
        System.out.println(command3);
        System.out.println(command4);

        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + command1 + " && " + command2);
            //Process process = Runtime.getRuntime().exec("cmd /c " + command3 + " && " + command4);
            InputStream fis = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while((line = br.readLine()) != null)
                System.out.println(line);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void addDependencyToPom(String projectDir, String jarPath) {
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
            systemPathNode.setText(jarPath);

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
