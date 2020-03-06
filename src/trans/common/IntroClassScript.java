package trans.common;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class IntroClassScript {
    public static void Crawl(String IntroClassPath, String MutationPath) {
        String DataPath = IntroClassPath + "\\dataset";

        File DataFile = new File(DataPath);
        File[] projectFiles = DataFile.listFiles();
        if (projectFiles == null) return;

        for (File projectFile : projectFiles)
            if (projectFile.isDirectory()) {
                File[] subjectFiles = projectFile.listFiles();
                if (subjectFiles == null) continue;
                for (File subjectFile : subjectFiles)
                    if (subjectFile.isDirectory() && !subjectFile.getName().equals("ref")) {
                        String outPath = subjectFile.getAbsolutePath() + "\\output.txt";
                        File outFile = new File(outPath);
                        if (outFile.exists())
                            outFile.delete();
                        File[] bugFiles = subjectFile.listFiles();
                        if (bugFiles == null) continue;
                        for (File bugFile : bugFiles) {
                            /*if(!projectFile.getName().equals("smallest"))
                                continue;
                            if(!subjectFile.getName().equals("818f8cf4e2e713753d02db9ee70a099b71f2a5a6bdc904191cf9ba68cfa5f64328464dccdd9b02fe0822e14a403dc196fe88b9964969409e60c93a776186a86a"))
                                continue;
                            if(!bugFile.getName().equals("003"))
                                continue;*/

                            String bugRelativePath = bugFile.getAbsolutePath().substring(DataFile.getAbsolutePath().length());
                            String javaFileName = projectFile.getName() + "_" + subjectFile.getName().substring(0, 8) + "_" + bugFile.getName() + ".java";
                            String javaFilePath = bugFile.getAbsolutePath() + "\\src\\main\\java\\introclassJava\\" + javaFileName;
                            File javaFile = new File(javaFilePath);
                            if (!javaFile.exists())
                                LevelLogger.error("Cannot Find Java Source : " + javaFilePath);

                            File mutationDirectoryFile = new File(MutationPath + "\\dataset" + bugRelativePath + "\\mutations");
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
                                        write(mutationFile.getName() + "\n" + runTest(bugFile.getAbsolutePath()) + "------\n", outPath);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            write("######\n", outPath);
                        }
                    }
            }
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

    public static void write(String string, String filePath) {
        File file = new File(filePath);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
            bufferedWriter.write(string);
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException ignored) {
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
