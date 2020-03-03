package trans.common;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import trans.dynamicUtils.DynamicInfo;
import trans.dynamicUtils.DynamicParser;
import trans.staticUtils.StaticInfo;
import trans.staticUtils.StaticParser;
import trans.trace.TraceParser;

import java.io.*;
import java.util.ArrayList;

public class Transverse {
    public static void run(String jsonFile) {
        String jsonSource = Util.readFileToString(jsonFile);
        JSONObject rootObject = (JSONObject) JSONValue.parse(jsonSource);
        JSONArray data = (JSONArray) rootObject.get("data");
        for (Object obj : data) {
            JSONObject jsonData = (JSONObject) obj;

            String projectPath = (String) jsonData.get("project");
            String srcFilePath = (String) jsonData.get("source");
            String testName = (String) jsonData.get("test");
            String methodName = (String) jsonData.get("method");
            JSONArray entryJSON = (JSONArray) jsonData.get("entry");
            JSONArray retJSON = (JSONArray) jsonData.get("return");

            ArrayList<String> exEntry = new ArrayList<>();
            for (Object entry : entryJSON) {
                JSONObject entryData = (JSONObject) entry;
                exEntry.add((String) entryData.get("var"));
            }

            ArrayList<String> exRet = new ArrayList<>();
            for (Object ret : retJSON) {
                JSONObject retData = (JSONObject) ret;
                exRet.add((String) retData.get("var"));
            }

            System.out.println("projectPath : " + projectPath);
            System.out.println("srcFilePath : " + srcFilePath);
            System.out.println("testName : " + testName);
            System.out.println("methodName : " + methodName);
            System.out.println("exEntry : " + exEntry);
            System.out.println("exRet : " + exRet);
            System.out.println("---------------------------------------------");

            Double probability = Transverse.run(projectPath, srcFilePath, testName, methodName, exEntry, exRet);
            System.out.println(probability);
        }
    }

    public static double run(String originProjectPath, String srcFilePath, String testName, String methodName,
                             ArrayList<String> exEntry, ArrayList<String> exRet) {
        copyProject(originProjectPath, Util.COPY_PROJECT_PATH);
        StaticInfo stcInfo = StaticParser.Analyze(originProjectPath + srcFilePath, methodName);
        String traceCode = TraceParser.Analyze(originProjectPath + srcFilePath, methodName);
        Util.print(traceCode, Util.COPY_PROJECT_PATH + srcFilePath);
        addDependencyToPom(Util.COPY_PROJECT_PATH);
        runTest(Util.COPY_PROJECT_PATH, testName);
        DynamicInfo dycInfo = DynamicParser.Analyze(Util.JSON_FILE_PATH, stcInfo, exEntry, exRet);
        Util.print(dycInfo.genFigaroSource(), Util.FIGARO_FILE_PATH);
        return runFigaroProgram(Util.COPY_PROJECT_PATH);
    }

    private static void copyProject(String projectPath, String copyPath) {
        File copyDir = new File(copyPath);
        if (copyDir.exists())
            Util.deleteDir(copyPath);

        String command = "xcopy " + projectPath + " " + copyPath + " /E /I /H /C /Y";
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
        String command1 = "scalac -cp " + Util.FIGARO_JAR_PATH + " -d " + workPath + " " + sourcePath;
        String command2 = "scala -cp " + workPath + ";" + Util.FIGARO_JAR_PATH + " patch";
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
            systemPathNode.setText(Util.JAVA2FIGARO_JAR_PATH);

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
}
