package trans.common;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;

public class IntroClassScript {
    public static void parseResult(String resultPath) {
        File resultDir = new File(resultPath);
        if ((!resultDir.exists()) || (!resultDir.isDirectory()))
            return;
        File[] resultFiles = resultDir.listFiles();
        if (resultFiles == null)
            return;

        for (File resultFile : resultFiles) {
            if (!resultFile.getName().endsWith(".txt")) continue;
            try {
                File outFile = new File(resultPath, "AvailablePatch_" + resultFile.getName());
                if (outFile.exists())
                    outFile.delete();

                InputStream in = new FileInputStream(resultFile);
                InputStreamReader inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;

                String bugID = null;
                String patch = null;
                boolean repairTest = false;
                HashSet<String> originFailTest = new HashSet<>();
                HashSet<String> patchFailTest = new HashSet<>();
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.length() == 0)
                        continue;
                    if (line.charAt(0) == '\\') {
                        String[] tags = line.split("\\\\");
                        bugID = tags[1] + "_" + tags[2].substring(0, 8) + "_" + tags[3];
                        System.out.println(bugID);
                    }
                    else if (line.contains("Origin")) {
                        if (repairTest)
                            Util.write("\n--------------\n", outFile, true);
                        patch = "Origin";
                        repairTest = false;
                        originFailTest.clear();
                        patchFailTest.clear();
                    }
                    else if (getNumber(line) != null) {
                        patch = line;
                        patchFailTest.clear();
                    }
                    else if(line.endsWith("Test")) {
                        if (patch.equals("Origin"))
                            originFailTest.add(line);
                        else
                            patchFailTest.add(line);
                    }
                    else if(line.contains("BUILD SUCCESS")) {
                        HashSet<String> patchSuccessTest = new HashSet<>(originFailTest);
                        patchSuccessTest.removeAll(patchFailTest);
                        if (patchSuccessTest.size() > 0) {
                            Util.write(bugID + " " + patch, outFile, true);
                            repairTest = true;
                        }
                    }
                    else if ((line.contains("BUILD FAILURE")) || ((line.charAt(0) == '#'))) {
                        if (patchFailTest.size() == 0)
                            continue;

                        HashSet<String> patchSuccessTest = new HashSet<>(originFailTest);
                        patchSuccessTest.removeAll(patchFailTest);
                        if (patchSuccessTest.size() > 0) {
                            Util.write(bugID + " " + patch, outFile, true);
                            repairTest = true;
                        }
                    }
                }

                inputStreamReader.close();
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void crawl(String IntroClassPath, String MutationPath) {
        File DataFile = new File(IntroClassPath, "dataset");
        File[] projectFiles = DataFile.listFiles();
        if (projectFiles == null) return;

        for (File projectFile : projectFiles)
            if (projectFile.isDirectory()) {
                File[] subjectFiles = projectFile.listFiles();
                if (subjectFiles == null) continue;
                for (File subjectFile : subjectFiles)
                    if (subjectFile.isDirectory() && !subjectFile.getName().equals("ref")) {
                        File[] bugFiles = subjectFile.listFiles();
                        if (bugFiles == null) continue;
                        for (File bugFile : bugFiles) {
                            if(!projectFile.getName().equals("smallest"))
                                continue;
                            String outPath = MutationPath + "\\output\\" + subjectFile.getName().substring(0, 8) + "_" + bugFile.getName() + ".txt";
                            File outFile = new File(outPath);
                            if (outFile.exists())
                                outFile.delete();
                            /*if(!subjectFile.getName().equals("818f8cf4e2e713753d02db9ee70a099b71f2a5a6bdc904191cf9ba68cfa5f64328464dccdd9b02fe0822e14a403dc196fe88b9964969409e60c93a776186a86a"))
                                continue;
                            if(!bugFile.getName().equals("003"))
                                continue;*/

                            String bugRelativePath = bugFile.getAbsolutePath().substring(DataFile.getAbsolutePath().length());
                            String javaFileName = projectFile.getName() + "_" + subjectFile.getName().substring(0, 8) + "_" + bugFile.getName() + ".java";
                            String javaFilePath = bugFile.getAbsolutePath() + "\\src\\main\\java\\introclassJava\\" + javaFileName;
                            File javaFile = new File(javaFilePath);
                            if (!javaFile.exists())
                                LevelLogger.error("Cannot Find Java Source : " + javaFilePath);

                            Util.write(bugRelativePath + "\nOrigin\n" + runTest(bugFile.getAbsolutePath()) + "------\n", outFile, true);
                            File[] mutationFiles = getMutationFile(MutationPath, subjectFile.getName(), bugFile.getName(), javaFileName);
                            if (mutationFiles == null)
                                continue;
                            for (File mutationJavaFile : mutationFiles) {
                                try {
                                    File originJavaFile = new File(javaFilePath);
                                    if (originJavaFile.exists())
                                        originJavaFile.delete();
                                    Files.copy(mutationJavaFile.toPath(), originJavaFile.toPath());
                                    Util.write(mutationJavaFile.getAbsolutePath() + "\n" + runTest(bugFile.getAbsolutePath()) + "------\n", outFile, true);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            Util.write("######\n", outFile, true);

                            /*File mutationDirectoryFile = new File(MutationPath + "\\dataset" + bugRelativePath + "\\mutations");
                            if (!mutationDirectoryFile.exists()) continue;
                            File[] mutationFiles = mutationDirectoryFile.listFiles();
                            if (mutationFiles == null) continue;

                            write(bugRelativePath + "\nOrigin\n" + runTest(bugFile.getAbsolutePath()) + "------\n", outPath);
                            for (File mutationFile : mutationFiles) {
                                File mutationJavaFile = new File(mutationFile.getAbsolutePath() + "\\" + javaFileName);
                                if (mutationJavaFile.exists()) {
                                    //if (Integer.parseInt(mutationFile.getName()) > 5) continue;
                                    try {
                                        File originJavaFile = new File(javaFilePath);
                                        if (originJavaFile.exists())
                                            originJavaFile.delete();
                                        Files.copy(mutationJavaFile.toPath(), originJavaFile.toPath());
                                        write(mutationFile.getName() + "\n" + runTest(bugFile.getAbsolutePath()) + "------\n", outFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            write("######\n", outPath);*/
                        }
                    }
            }
    }

    private static File[] getMutationFile(String MutationPath, String subject, String bugID, String javaFileName) {
        File bugDirectory = new File(MutationPath + "\\smallest_part1\\" + subject + "\\" + bugID);
        if (!bugDirectory.exists())
            bugDirectory = new File(MutationPath + "\\smallest_part2\\" + subject + "\\" + bugID);
        if (!bugDirectory.exists())
            bugDirectory = new File(MutationPath + "\\smallest_part3\\" + subject + "\\" + bugID);
        if (!bugDirectory.exists())
            bugDirectory = new File(MutationPath + "\\smallest_part4\\" + subject + "\\" + bugID);
        if (!bugDirectory.exists())
            return null;

        ArrayList<File> files = new ArrayList<>();
        String[] dirs = {"angelixAngelix2sc213", "angelixAngelix3sc213", "angelixAngelix4sc213", "angelixAngelix213",
                "angelixAngelix2213", "angelixAngelix3213", "angelixAngelix4213", "angelixangelixMaxSat1511",
                "angelixangelixMaxSat15112", "angelixCVC4213", "angelixEnum213", "angelixMeta213", "angelixMetaXts213"};
        for (String dir : dirs) {
            File file = new File(bugDirectory.getAbsolutePath() + "\\" + dir + "\\output\\introclassJava\\" + javaFileName);
            if (file.exists())
                files.add(file);
        }
        return files.toArray(new File[0]);
    }

    private static String runTest(String projectPath) {
        String command;
        if (projectPath.contains("D:"))
            command = "D: && cd " + projectPath + " && mvn clean test";
        else if (projectPath.contains("E:"))
            command = "E: && cd " + projectPath + " && mvn clean test";
        else if (projectPath.contains("F:"))
            command = "F: && cd " + projectPath + " && mvn clean test";
        else
            command = "cd " + projectPath + " && mvn clean test";

        StringBuilder out = new StringBuilder();
        LevelLogger.debug("RUN TEST : " + command);
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + command);
            InputStream fis = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            boolean flag = false;
            while((line = br.readLine()) != null) {
                if (line.contains("Tests run")) {
                    flag = false;
                }
                if (line.contains("Failed tests:")) {
                    flag = true;
                    line = line.substring(line.indexOf(":"));
                }
                if (line.contains("BUILD FAILURE"))
                    out.append("BUILD FAILURE").append("\n");
                if (line.contains("BUILD SUCCESS"))
                    out.append("BUILD SUCCESS").append("\n");

                if (!flag) continue;
                int beginIndex = line.indexOf('t');
                int midIndex = line.indexOf('(');
                int endIndex = line.indexOf(')');
                if ((beginIndex == -1) || (midIndex == -1) || (endIndex == -1)) continue;
                out.append(line.substring(beginIndex, midIndex)).append(" ").append(line.substring(midIndex + 1, endIndex)).append("\n");
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    private static Integer getNumber(String string) {
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
}
