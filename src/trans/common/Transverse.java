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
    public static void runByJsonFile(File workDirectory) {
        File jsonFile = new File(workDirectory, "WorkList.json");
        File outputFile = new File(workDirectory, "Output.txt");

        String jsonSource = Util.readFileToString(jsonFile);
        JSONObject rootObject = (JSONObject) JSONValue.parse(jsonSource);
        String jsonType = (String) rootObject.get("type");
        JSONArray dataJsonList = (JSONArray) rootObject.get("data");
        if (jsonType.equals("Project List"))
            runJsonFileByProjectList(workDirectory, dataJsonList, outputFile);
        else if (jsonType.equals("Project Mutants"))
            runByProjectMutants(workDirectory, dataJsonList, outputFile);
    }

    private static void runByProjectMutants(File workDirectory, JSONArray dataJsonList, File outputFile) {
        File copyProject = new File(workDirectory, "copy");
        File patchSimOutput = new File(workDirectory, "PatchSim.txt");
        for (Object obj : dataJsonList) {
            JSONObject jsonData = (JSONObject) obj;

            String projectName = (String) jsonData.get("project");
            String srcFilePath = (String) jsonData.get("source");
            String methodName = (String) jsonData.get("method");
            JSONArray entryJSONList = (JSONArray) jsonData.get("entry");
            JSONArray retJSONList = (JSONArray) jsonData.get("return");

            ArrayList<String> exEntryList = new ArrayList<>();
            for (Object entry : entryJSONList) {
                JSONObject entryData = (JSONObject) entry;
                exEntryList.add((String) entryData.get("var"));
            }

            ArrayList<String> exRetList = new ArrayList<>();
            for (Object ret : retJSONList) {
                JSONObject retData = (JSONObject) ret;
                exRetList.add((String) retData.get("var"));
            }

            //StringBuilder bugInformation = new StringBuilder();
            //bugInformation.append("project : ").append(projectName);
            //bugInformation.append("srcFilePath : ").append(srcFilePath);
            //bugInformation.append("methodName : ").append(methodName);
            //bugInformation.append("exEntryList : ").append(exEntryList);
            //bugInformation.append("exRetList : ").append(exRetList);
            //bugInformation.append("---------------------------------------------");
            //LevelLogger.debug(bugInformation);
            LevelLogger.debug("project : " + projectName);

            File projectDirectory = new File(workDirectory, projectName);
            File mutantDirectory = new File(projectDirectory, "mutants");
            File sourceDirectory = new File(projectDirectory, "source");
            if ((!mutantDirectory.exists()) || (!sourceDirectory.exists())) continue;
            File[] mutantFileList = mutantDirectory.listFiles();
            if (mutantFileList == null) continue;

            //--------Get Test Lists---------
            ArrayList<String> testList = getTestCases(sourceDirectory, copyProject);
            LevelLogger.debug(testList.toString());

            LevelLogger.debug("Project : " + projectName);
            StringBuilder outputString = new StringBuilder();
            outputString.append(projectName);
            ArrayList<String> testFailList = new ArrayList<>();
            ArrayList<Double> originResultList = new ArrayList<>();
            ArrayList<TraceList> originTraceLists = new ArrayList<>();
            //--------Get Origin Project Result-----
            for (String test : testList) {
                TestCaseResult testCaseResult = runProjectTestCase(sourceDirectory, copyProject, srcFilePath, test, methodName, exEntryList, exRetList);
                LevelLogger.debug("Origin " + test + " : " + String.format("%.6f", testCaseResult._probability * 100.0) + "%");
                originResultList.add(testCaseResult.getProbability() * Util.ALPHA);
                originTraceLists.add(testCaseResult.getTraceList());
                if (!testCaseResult.checkPass()) {
                    outputString.append(" ").append(testCaseResult.getProbability());
                    testFailList.add(test);
                }
            }
            outputString.append("\n");

            for (File mutantFile : mutantFileList) {
                if (!mutantFile.getName().endsWith(".java"))
                    continue;

                ArrayList<Double> patchResultList = new ArrayList<>();
                ArrayList<TraceList> patchTraceLists = new ArrayList<>();
                LevelLogger.debug("MutantFile : " + mutantFile.getName());
                outputString.append(mutantFile.getName());
                for (String test : testList) {
                    TestCaseResult testCaseResult = runMutantTestCase(sourceDirectory, copyProject, srcFilePath, mutantFile, test, methodName, exEntryList, exRetList);
                    LevelLogger.debug(mutantFile.getName() + " " + test + " : " + String.format("%.6f", testCaseResult._probability * 100.0) + "%");
                    patchResultList.add(testCaseResult.getProbability());
                    patchTraceLists.add(testCaseResult.getTraceList());
                    if (testFailList.contains(test))
                        outputString.append(" ").append(testCaseResult.getProbability());
                }
                outputString.append(" ").append(genMultiTestResult(originResultList, patchResultList)).append("\n");
                String patchSimResult = PatchSimParser.analyze(originTraceLists, patchTraceLists) ? "Y" : "N";
                Util.write(mutantFile.getName() + " " + patchSimResult, patchSimOutput, true);
            }
            Util.write(outputString.toString(), outputFile, true);
        }
    }

    private static void runJsonFileByProjectList(File workDirectory, JSONArray data, File outputFile) {
        File copyProject = new File(workDirectory, "copy");
        StringBuilder outputString = new StringBuilder();
        for (Object obj : data) {
            JSONObject jsonData = (JSONObject) obj;

            String projectPath = (String) jsonData.get("project");
            String srcFilePath = (String) jsonData.get("source");
            String testName = (String) jsonData.get("test");
            String methodName = (String) jsonData.get("method");
            JSONArray entryJSONList = (JSONArray) jsonData.get("entry");
            JSONArray retJSONList = (JSONArray) jsonData.get("return");

            ArrayList<String> exEntryList = new ArrayList<>();
            for (Object entry : entryJSONList) {
                JSONObject entryData = (JSONObject) entry;
                exEntryList.add((String) entryData.get("var"));
            }

            ArrayList<String> exRetList = new ArrayList<>();
            for (Object ret : retJSONList) {
                JSONObject retData = (JSONObject) ret;
                exRetList.add((String) retData.get("var"));
            }

            //StringBuilder bugInformation = new StringBuilder();
            //bugInformation.append("projectPath : ").append(projectPath);
            //bugInformation.append("srcFilePath : ").append(srcFilePath);
            //bugInformation.append("testName : ").append(testName);
            //bugInformation.append("methodName : ").append(methodName);
            //bugInformation.append("exEntryList : ").append(exEntryList);
            //bugInformation.append("exRetList : ").append(exRetList);
            //bugInformation.append("---------------------------------------------");
            //LevelLogger.debug(bugInformation);

            File projectDirectory = new File(projectPath);
            TestCaseResult testCaseResult = Transverse.runProjectTestCase(projectDirectory, copyProject, srcFilePath, testName, methodName, exEntryList, exRetList);
            outputString.append(projectDirectory.getName())
                    .append(" : ")
                    .append(String.format("%.4f", testCaseResult.getProbability() * 100.0))
                    .append("%\n");
        }
        Util.write(outputString.toString(), outputFile, false);
    }

    static TestCaseResult runMutantTestCase(File originProject, File copyProject, String srcFilePath, File mutantFile, String testName, String methodName,
                                            ArrayList<String> exEntry, ArrayList<String> exRet) {
        //-----Copy Project-----
        File copySourceFile = new File(copyProject, srcFilePath);
        File dumpFile = new File(copyProject, "DumpResult.json");
        File figaroFile = new File(copyProject, "patch.scala");
        copyProject(originProject, copyProject);
        Util.copyFile(mutantFile, copySourceFile);

        //-----Run Trace-----
        StaticInfo stcInfo = StaticParser.analyze(copySourceFile, methodName);
        String traceCode = TraceParser.analyze(copySourceFile, methodName);
        Util.write(traceCode, copySourceFile, false);
        addDependencyToPom(copyProject);
        boolean testResult = runTest(copyProject, testName);

        //-----Generate Figaro Result-----
        DynamicInfo dycInfo = DynamicParser.analyze(dumpFile, stcInfo, exEntry, exRet);
        Util.write(dycInfo.genFigaroSource(), figaroFile, false);
        double probability = runFigaroProgram(copyProject);
        return new TestCaseResult(dycInfo, probability, testResult);
    }

    static TestCaseResult runProjectTestCase(File originProject, File copyProject, String srcFilePath, String testName, String methodName,
                                            ArrayList<String> exEntry, ArrayList<String> exRet) {
        //-----Copy Project-----
        File copySourceFile = new File(copyProject, srcFilePath);
        File dumpFile = new File(copyProject, "DumpResult.json");
        File figaroFile = new File(copyProject, "patch.scala");
        copyProject(originProject, copyProject);

        //-----Run Trace-----
        StaticInfo stcInfo = StaticParser.analyze(copySourceFile, methodName);
        String traceCode = TraceParser.analyze(copySourceFile, methodName);
        Util.write(traceCode, copySourceFile, false);
        addDependencyToPom(copyProject);
        boolean testResult = runTest(copyProject, testName);

        //-----Generate Figaro Result-----
        DynamicInfo dycInfo = DynamicParser.analyze(dumpFile, stcInfo, exEntry, exRet);
        Util.write(dycInfo.genFigaroSource(), figaroFile, false);
        double probability = runFigaroProgram(copyProject);
        return new TestCaseResult(dycInfo, probability, testResult);
    }

    public static void genMultiTestResultByFiles (File resultFile) {
        String resultString = Util.readFileToString(resultFile);
        String[] resultList = resultString.split("\n");
        String patchID = "Origin";
        ArrayList<Double> originTestResultList = new ArrayList<>();
        ArrayList<Double> patchTestResultList = new ArrayList<>();

        for (String line : resultList) {
            if (line.contains("#")) {
                String resultValueString = line.split(" ")[2].split("%")[0];
                double value = Double.parseDouble(resultValueString) / 100.0;
                if (patchID.equals("Origin"))
                    originTestResultList.add(value);
                else
                    patchTestResultList.add(value);
            }
            if (line.contains(".java")) {
                if (patchTestResultList.size() == originTestResultList.size())
                    System.out.println("Patch " + patchID + " Final Result : " + genMultiTestResult(originTestResultList, patchTestResultList));
                patchTestResultList.clear();
                patchID = line.split("\\.")[0];
            }
        }
        if (patchTestResultList.size() == originTestResultList.size())
            System.out.println("Patch " + patchID + " Final Result : " + genMultiTestResult(originTestResultList, patchTestResultList));
    }

    private static double genMultiTestResult(ArrayList<Double> originResultList, ArrayList<Double> patchResultList) {
        int numTest = originResultList.size();
        //LevelLogger.debug("Origin " + originResultList.toString());
        //LevelLogger.debug("patch " + patchResultList.toString());
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

    private static void copyProject(File project, File target) {
        if (target.exists())
            Util.deleteDir(target);

        String command = "xcopy " + project.getAbsolutePath() + " " + target.getAbsolutePath() + " /E /I /H /C /Y";
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

        boolean checkPass()
        {
            return _isPass;
        }

        TraceList getTraceList() {
            if (_dynamicInfo != null)
                return _dynamicInfo.genTraceList(_isPass);
            return null;
        }

        double getProbability() {
            return _probability;
        }
    }

    private static boolean runTest(File project, String testName) {
        String projectPath = project.getAbsolutePath();
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

    private static double runFigaroProgram(File workDirectory) {
        double retValue = 0;
        String sourcePath = new File(workDirectory, "patch.scala").getAbsolutePath();
        String command1 = "scalac -cp " + Util.FIGARO_JAR_FILE.getAbsolutePath() + " -d " + workDirectory.getAbsolutePath() + " " + sourcePath;
        String command2 = "scala -cp " + workDirectory.getAbsolutePath() + ";" + Util.FIGARO_JAR_FILE.getAbsolutePath() + " patch";
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

    private static ArrayList<String> getTestCases(File sourceDirectory, File projectDirectory) {
        copyProject(sourceDirectory, projectDirectory);
        runTest(projectDirectory, null);

        ArrayList<String> testList = new ArrayList<>();
        File targetDirectory = new File(projectDirectory, "target");
        File reportDirectory = new File(targetDirectory, "surefire-reports");
        if (!reportDirectory.exists())
            return testList;
        File[] reportFiles = reportDirectory.listFiles();
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

    private static void addDependencyToPom(File project) {
        try {
            File pomFile = new File(project, "pom.xml");
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
            systemPathNode.setText(Util.JAVA2FIGARO_JAR_FILE.getAbsolutePath());

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
