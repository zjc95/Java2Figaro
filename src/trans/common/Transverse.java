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
import trans.patchsim.PatchSimParser;
import trans.patchsim.TraceList;
import trans.staticUtils.StaticInfo;
import trans.staticUtils.StaticParser;
import trans.trace.TraceParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
        StringBuilder finalResult = new StringBuilder();
        for (Object obj : data) {
            JSONObject jsonData = (JSONObject) obj;

            String projectName = (String) jsonData.get("project");
            String srcFilePath = (String) jsonData.get("source");
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

            //--------Get Test Lists---------
            copyProject(sourceDirPath, Util.COPY_PROJECT_PATH);
            runTest(Util.COPY_PROJECT_PATH, null);
            ArrayList<String> testList = getTestCases(Util.COPY_PROJECT_PATH);
            LevelLogger.debug(testList.toString());

            LevelLogger.debug("Project : " + projectName);
            finalResult.append("Project : ").append(projectName).append("\n");
            ArrayList<Double> originResultList = new ArrayList<>();
            ArrayList<TraceList> originTraceListArray = new ArrayList<>();
            //--------Get Origin Project Result-----
            for (String test : testList) {
                TestCaseResult testCaseResult = runProjectTestCase(sourceDirPath, srcFilePath, test, methodName, exEntry, exRet);
                LevelLogger.debug("Origin " + test + " : " + String.format("%.6f", testCaseResult._probability * 100.0) + "%");
                originResultList.add(testCaseResult.getProbability());
                originTraceListArray.add(testCaseResult.getTraceList());
            }

            for (File mutantFile : mutantFiles) {
                if (!mutantFile.getName().endsWith(".java"))
                    continue;

                ArrayList<Double> patchResultList = new ArrayList<>();
                ArrayList<TraceList> patchTraceListArray = new ArrayList<>();
                LevelLogger.debug("MutantFile : " + mutantFile.getName());
                for (String test : testList) {
                    TestCaseResult testCaseResult = runMutantTestCase(sourceDirPath, srcFilePath, mutantFile, test, methodName, exEntry, exRet);
                    LevelLogger.debug(mutantFile.getName() + " " + test + " : " + String.format("%.6f", testCaseResult._probability * 100.0) + "%");
                    patchResultList.add(testCaseResult.getProbability());
                    patchTraceListArray.add(testCaseResult.getTraceList());
                }
                finalResult.append("Patch ").append(mutantFile.getName()).append(" : ").append(genMultiTestResult(originResultList, patchResultList)).append("\n");
                boolean patchSimResult = PatchSimParser.analyze(originTraceListArray, patchTraceListArray);
                finalResult.append("Patch-Sim Result : ").append(patchSimResult ? "correct" : "incorrect").append("\n");
            }
        }
        Util.print(finalResult.toString(), outputPath);
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

            TestCaseResult testCaseResult = Transverse.runProjectTestCase(projectPath, srcFilePath, testName, methodName, exEntry, exRet);

            String[] projectPathFragment = projectPath.split("\\\\");
            String projectName = projectPathFragment[projectPathFragment.length - 1];
            output.append(projectName).append(" : ").append(String.format("%.4f", testCaseResult.getProbability() * 100.0)).append("%\n");
        }
        Util.print(output.toString(), outputPath);
    }

    private static TestCaseResult runMutantTestCase(String originProjectPath, String srcFilePath, File mutantFile, String testName, String methodName,
                                            ArrayList<String> exEntry, ArrayList<String> exRet) {
        //-----Copy Project-----
        String copySrcFilePath = Util.COPY_PROJECT_PATH + srcFilePath;
        copyProject(originProjectPath, Util.COPY_PROJECT_PATH);
        Util.copyFile(mutantFile, copySrcFilePath);

        //-----Run Trace-----
        StaticInfo stcInfo = StaticParser.Analyze(copySrcFilePath, methodName);
        String traceCode = TraceParser.analyze(copySrcFilePath, methodName);
        Util.print(traceCode, copySrcFilePath);
        addDependencyToPom(Util.COPY_PROJECT_PATH);
        boolean testResult = runTest(Util.COPY_PROJECT_PATH, testName);

        //-----Generate Figaro Result-----
        DynamicInfo dycInfo = DynamicParser.Analyze(Util.JSON_FILE_PATH, stcInfo, exEntry, exRet);
        Util.print(dycInfo.genFigaroSource(), Util.FIGARO_FILE_PATH);
        double probability = runFigaroProgram(Util.COPY_PROJECT_PATH);
        return new TestCaseResult(dycInfo, probability, testResult);
    }

    public static TestCaseResult runProjectTestCase(String originProjectPath, String srcFilePath, String testName, String methodName,
                                            ArrayList<String> exEntry, ArrayList<String> exRet) {
        //-----Copy Project-----
        String copySrcFilePath = Util.COPY_PROJECT_PATH + srcFilePath;
        copyProject(originProjectPath, Util.COPY_PROJECT_PATH);

        //-----Run Trace-----
        StaticInfo stcInfo = StaticParser.Analyze(copySrcFilePath, methodName);
        String traceCode = TraceParser.analyze(copySrcFilePath, methodName);
        Util.print(traceCode, Util.COPY_PROJECT_PATH + srcFilePath);
        addDependencyToPom(Util.COPY_PROJECT_PATH);
        boolean testResult = runTest(Util.COPY_PROJECT_PATH, testName);

        //-----Generate Figaro Result-----
        DynamicInfo dycInfo = DynamicParser.Analyze(Util.JSON_FILE_PATH, stcInfo, exEntry, exRet);
        Util.print(dycInfo.genFigaroSource(), Util.FIGARO_FILE_PATH);
        double probability = runFigaroProgram(Util.COPY_PROJECT_PATH);
        return new TestCaseResult(dycInfo, probability, testResult);
    }

    public static void genMultiTestResultByFiles (String resultFilePath) {
        String result = Util.readFileToString(resultFilePath);
        String[] resultList = result.split("\n");
        String patchID = "Origin";
        ArrayList<Double> originTestResult = new ArrayList<>();
        ArrayList<Double> patchTestResult = new ArrayList<>();

        for (String line : resultList) {
            if (line.contains("#")) {
                String resultValueString = line.split(" ")[2].split("%")[0];
                double value = Double.parseDouble(resultValueString) / 100.0;
                if (patchID.equals("Origin"))
                    originTestResult.add(value);
                else
                    patchTestResult.add(value);
            }
            if (line.contains(".java")) {
                if (patchTestResult.size() == originTestResult.size())
                    System.out.println("Patch " + patchID + " Final Result : " + genMultiTestResult(originTestResult, patchTestResult));
                patchTestResult.clear();
                patchID = line.split("\\.")[0];
            }
        }
        if (patchTestResult.size() == originTestResult.size())
            System.out.println("Patch " + patchID + " Final Result : " + genMultiTestResult(originTestResult, patchTestResult));
    }

    private static double genMultiTestResult(ArrayList<Double> originResultList, ArrayList<Double> patchResultList) {
        int numTest = originResultList.size();
        //System.out.println("Origin " + originResultList.toString());
        //System.out.println("patch " + patchResultList.toString());
        double result = 0.0;
        for (int binary = 0; binary < (1 << (numTest * 2)); binary++) {
            double rate = 1.0;
            int numOriginPass = 0;
            int numPatchPassWhenOriginFail = 0;
            int numPatchFailWhenOriginPass = 0;

            for (int i = 0; i < numTest; i++) {
                if ((binary & (1 << i)) > 0) {
                    rate *= originResultList.get(i);
                    numOriginPass++;
                    if ((binary & (1 << (i + numTest))) == 0)
                        numPatchFailWhenOriginPass++;
                }
                else {
                    rate *= 1 - originResultList.get(i);
                    if ((binary & (1 << (i + numTest))) > 0)
                        numPatchPassWhenOriginFail++;
                }

                if ((binary & (1 << (i + numTest))) > 0)
                    rate *= patchResultList.get(i);
                else
                    rate *= 1 - patchResultList.get(i);
            }
            //System.out.println(binary + " " + numOriginPass + " " + numPatchPassWhenOriginFail + " " + numPatchFailWhenOriginPass);

            double tmpValue1 = numOriginPass == numTest ? 0.0 : (double) numPatchPassWhenOriginFail / (numTest - numOriginPass);
            double tmpValue2 = numOriginPass == 0 ? 0.0 : (double) numPatchFailWhenOriginPass / numOriginPass;
            result += rate * (tmpValue1 - Util.ALPHA * tmpValue2);
        }
        return result;
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

    static class TestCaseResult {
        private DynamicInfo _dynamicInfo;
        private double _probability;
        private boolean _isPass;

        TestCaseResult(DynamicInfo dynamicInfo, double probability, boolean isPass) {
            _dynamicInfo = dynamicInfo;
            _probability = probability;
            _isPass = isPass;
        }

        TraceList getTraceList() {
            if (_dynamicInfo != null)
                return _dynamicInfo.genTraceList(_isPass);
            return null;
        }

        double getProbability() {
            if (_isPass) return _probability;
            return 0.0;
        }
    }

    private static boolean runTest(String projectPath, String testName) {
        String command;
        switch (projectPath.charAt(0)) {
            case 'D' : command = "D: && cd " + projectPath + "&& mvn test"; break;
            case 'E' : command = "E: && cd " + projectPath + "&& mvn test"; break;
            case 'F' : command = "F: && cd " + projectPath + "&& mvn test"; break;
            case 'G' : command = "G: && cd " + projectPath + "&& mvn test"; break;
            default: command = "cd " + projectPath + "&& mvn test";
        }
        if (testName != null) {
            command += " -Dtest=" + testName;
            LevelLogger.debug("RUN TEST : " + testName);
        }
        boolean ret = false;
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + command);
            InputStream fis = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while((line = br.readLine()) != null) {
                if (line.contains("BUILD FAILURE"))
                    ret = false;
                if (line.contains("BUILD SUCCESS"))
                    ret = true;
                //LevelLogger.debug(line);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
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

    private static ArrayList<String> getTestCases(String projectPath) {
        ArrayList<String> testList = new ArrayList<>();
        File reportDir = new File(projectPath + "\\target\\surefire-reports");
        if (!reportDir.exists())
            return testList;
        File[] reportFiles = reportDir.listFiles();
        if (reportFiles == null)
            return testList;

        for (File file : reportFiles) {
            if (!file.getName().endsWith(".xml")) continue;
            try {
                Document document = new SAXReader().read(file);
                Element rootNode = document.getRootElement();
                List<Element> rootNodeList = rootNode.elements("testcase");
                for (Element element : rootNodeList){
                    String className = element.attributeValue("classname");
                    String testName = element.attributeValue("name");
                    testList.add(className + "#" + testName);
                }
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        return testList;
    }

    private static void addDependencyToPom(String projectPath) {
        try {
            File pomFile = new File(projectPath + "\\pom.xml");
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
