package trans.common;

import com.mysql.cj.xdevapi.JsonArray;
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
    public static void runJsonFile(String workPath) {
        String jsonFilePath = workPath + "\\WorkList.json";
        String outputPath = workPath + "\\Output.txt";

        String jsonSource = Util.readFileToString(jsonFilePath);
        JSONObject rootObject = (JSONObject) JSONValue.parse(jsonSource);
        String jsonType = (String) rootObject.get("type");
        JSONArray data = (JSONArray) rootObject.get("data");
        if (jsonType.equals("Project List"))
            runJsonFileByProjectList(data, outputPath);
        else if (jsonType.equals("Project Mutants"))
            runJsonFileByProjectMutants(workPath, data, outputPath);
    }

    private static void runJsonFileByProjectMutants(String workPath, JSONArray data, String outputPath) {
        StringBuilder output = new StringBuilder();
        for (Object obj : data) {
            JSONObject jsonData = (JSONObject) obj;

            String projectName = (String) jsonData.get("project");
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

            StringBuilder bugInformation = new StringBuilder();
            bugInformation.append("project : ").append(projectName);
            bugInformation.append("srcFilePath : ").append(srcFilePath);
            bugInformation.append("testName : ").append(testName);
            bugInformation.append("methodName : ").append(methodName);
            bugInformation.append("exEntry : ").append(exEntry);
            bugInformation.append("exRet : ").append(exRet);
            bugInformation.append("---------------------------------------------");
            //LevelLogger.debug(bugInformation);

            String mutantDirPath = workPath + "\\" + projectName + "\\mutants";
            String sourceDirPath = workPath + "\\" + projectName + "\\source";
            File mutantDir = new File (mutantDirPath);
            File sourceDir = new File (sourceDirPath);
            if ((!mutantDir.exists()) || (!sourceDir.exists()))
                continue;
            File[] mutantFiles = mutantDir.listFiles();
            if (mutantFiles == null)
                continue;

            LevelLogger.debug("project : " + projectName);
            for (File mutantFile : mutantFiles) {
                if (!mutantFile.getName().endsWith(".java"))
                    continue;
                LevelLogger.debug("MutantFile : " + mutantFile.getName());
                double probability = Transverse.runMutant(sourceDirPath, srcFilePath, mutantFile, testName, methodName, exEntry, exRet);
                LevelLogger.debug("Result " + mutantFile.getName() + " : " + String.format("%.4f", probability * 100.0) + "%");
                output.append(mutantFile.getName()).append(" : ").append(String.format("%.4f", probability * 100.0)).append("%\n");
            }
            output.append("\n");
        }
        Util.print(output.toString(), outputPath);
    }

    public static double runMutant(String originProjectPath, String srcFilePath, File mutantFile, String testName, String methodName,
                                    ArrayList<String> exEntry, ArrayList<String> exRet) {
        copyProject(originProjectPath, Util.COPY_PROJECT_PATH);
        Util.copyFile(mutantFile, Util.COPY_PROJECT_PATH + srcFilePath);
        StaticInfo stcInfo = StaticParser.Analyze(Util.COPY_PROJECT_PATH + srcFilePath, methodName);
        String traceCode = TraceParser.Analyze(Util.COPY_PROJECT_PATH + srcFilePath, methodName);
        Util.print(traceCode, Util.COPY_PROJECT_PATH + srcFilePath);
        addDependencyToPom(Util.COPY_PROJECT_PATH);
        runTest(Util.COPY_PROJECT_PATH, testName);
        DynamicInfo dycInfo = DynamicParser.Analyze(Util.JSON_FILE_PATH, stcInfo, exEntry, exRet);
        Util.print(dycInfo.genFigaroSource(), Util.FIGARO_FILE_PATH);
        return runFigaroProgram(Util.COPY_PROJECT_PATH);
    }

    private static void runJsonFileByProjectList(JSONArray data, String outputPath) {
        StringBuilder output = new StringBuilder();
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

            StringBuilder bugInformation = new StringBuilder();
            bugInformation.append("projectPath : ").append(projectPath);
            bugInformation.append("srcFilePath : ").append(srcFilePath);
            bugInformation.append("testName : ").append(testName);
            bugInformation.append("methodName : ").append(methodName);
            bugInformation.append("exEntry : ").append(exEntry);
            bugInformation.append("exRet : ").append(exRet);
            bugInformation.append("---------------------------------------------");
            //LevelLogger.debug(bugInformation);

            double probability = Transverse.runProject(projectPath, srcFilePath, testName, methodName, exEntry, exRet);

            String[] projectPathFragment = projectPath.split("\\\\");
            String projectName = projectPathFragment[projectPathFragment.length - 1];
            output.append(projectName).append(" : ").append(String.format("%.4f", probability * 100.0)).append("%\n");
        }
        Util.print(output.toString(), outputPath);
    }

    public static double runProject(String originProjectPath, String srcFilePath, String testName, String methodName,
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
        LevelLogger.debug("RUN TEST : " + testName);
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + command);
            InputStream fis = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            //String line;
            //while((line = br.readLine()) != null)
            //    LevelLogger.debug(line);
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
        LevelLogger.debug("GENERATE FIGARO RESULT");

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
