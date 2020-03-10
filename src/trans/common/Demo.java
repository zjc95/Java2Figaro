package trans.common;

import java.util.ArrayList;

public class Demo {

    public static void main(String[] args) {
        runJsonFile();
    }

    private static void runJsonFile() {
        String workPath = "D:\\program\\workspace\\Java2FigaroData";
        Transverse.runJsonFile(workPath);
    }

    private static void runProject1() {
        String originProjectPath = "D:\\program\\workspace\\Java2FigaroData2\\smallest_6aaeaf2f_001_1";
        String srcFilePath = "\\src\\main\\java\\introclassJava\\smallest_6aaeaf2f_001.java";
        String testName = "smallest_6aaeaf2f_001WhiteboxTest#test1";
        String methodName = "exec";

        ArrayList<String> exEntry = new ArrayList<>();
        ArrayList<String> exRet = new ArrayList<>();
        exEntry.add("scanner");
        exEntry.add("output");
        exRet.add("output");

        Double probability = Transverse.runProject(originProjectPath, srcFilePath, testName, methodName, exEntry, exRet);
        System.out.println(String.format("%.4f", probability * 100.0));
    }

    public static void runProject2() {
        String originProjectPath = "D:\\program\\workspace\\Java2FigaroData2\\smallest_6aaeaf2f_001_1";
        String srcFilePath = "\\src\\main\\java\\introclassJava\\smallest_6aaeaf2f_001.java";
        String testName = "smallest_6aaeaf2f_001WhiteboxTest#test1";
        String methodName = "exec";

        ArrayList<String> exEntry = new ArrayList<>();
        ArrayList<String> exRet = new ArrayList<>();
        exEntry.add("scanner");
        exEntry.add("output");
        exRet.add("output");

        Double probability = Transverse.runProject(originProjectPath, srcFilePath, testName, methodName, exEntry, exRet);
        System.out.println(String.format("%.4f", probability * 100.0));
    }

    public static void parseResult() {
        String resultPath = "D:\\program\\workspace\\CapGen\\CapGenOutput";
        IntroClassScript.parseResult(resultPath);
    }

    public static void crawlIntroClassS3Patch(String[] args) {
        String IntroClassPath = "D:\\program\\workspace\\IntroClassJavaCopy";
        String mutationPath = "D:\\program\\workspace\\FSE2017-S3-SyntaxSemanticRepairData-master";
        IntroClassScript.crawl(IntroClassPath, mutationPath);
    }
}
