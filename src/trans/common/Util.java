package trans.common;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class Util {
    public static final int JAVA_LEVEL = AST.JLS8;
    public static final double ALPHA = 0.8;
    public static final String JAVA_VERSION = JavaCore.VERSION_1_8;
    public static final String LIBRARY_PATH = System.getProperty("user.dir") + "\\lib";
    public static final String FIGARO_JAR_PATH = LIBRARY_PATH + "\\figaro_2.12-5.0.0.0.jar";
    public static final String JAVA2FIGARO_JAR_PATH = LIBRARY_PATH + "\\Java2Figaro.jar";
    public static final String WORK_PATH = System.getProperty("user.dir") + "\\resources\\test";
    public static final String COPY_PROJECT_PATH = WORK_PATH + "\\copy";
    public static final String FIGARO_FILE_PATH = COPY_PROJECT_PATH + "\\patch.scala";
    public static final String JSON_FILE_PATH = COPY_PROJECT_PATH + "\\DumpResult.json";

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

    public static void deleteDir(String dirPath)
    {
        File file = new File(dirPath);
        if(! file.isFile()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File value : files) {
                    deleteDir(value.getAbsolutePath());
                }
            }
        }
        file.delete();
    }

    public static void write(String string, String filePath, boolean isAppend) {
        File file = new File(filePath);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, isAppend), StandardCharsets.UTF_8));
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

    public static void copyFile(File sourceFile, String targetFilePath) {
        if (!sourceFile.isFile()) return;
        try {
            File targetFile = new File(targetFilePath);
            if (targetFile.exists())
                targetFile.delete();
            Files.copy(sourceFile.toPath(), targetFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
