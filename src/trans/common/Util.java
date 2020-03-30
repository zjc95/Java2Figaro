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
    public static final double ALPHA = 0.8;
    public static final double SEMANTIC_HIGH_PROBABILITY = 1.0;
    public static final double SEMANTIC_CONSTANT_PROBABILITY = 1.0;
    public static final double SEMANTIC_LOW_PROBABILITY = 0.05;
    public static final double SEMANTIC_BOOLEAN_PROBABILITY = 0.5;

    public static final double STRATEGY_HIGH_PROBABILITY = 0.95;
    public static final double STRATEGY_LOW_PROBABILITY = 0.8;


    public static final int JAVA_LEVEL = AST.JLS8;
    public static final String JAVA_VERSION = JavaCore.VERSION_1_8;
    public static final File LIBRARY_DIRECTORY = new File(System.getProperty("user.dir"),"lib");
    public static final File FIGARO_JAR_FILE = new File(LIBRARY_DIRECTORY, "figaro_2.12-5.0.0.0.jar");
    public static final File JAVA2FIGARO_JAR_FILE = new File(LIBRARY_DIRECTORY, "Java2Figaro.jar");

    public static CompilationUnit genASTFromSource(File srcFile, String srcPath) {
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
        astParser.setUnitName(srcFile.getName());
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

    public static String readFileToString(File file) {
        if (!file.exists() || !file.isFile()) {
            LevelLogger.error("#readFileToString Illegal input file path : " + file.getAbsolutePath());
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        InputStream in = null;
        InputStreamReader inputStreamReader = null;
        try {
            in = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            char[] ch = new char[1024];
            int readCount = 0;
            while ((readCount = inputStreamReader.read(ch)) != -1) {
                stringBuilder.append(ch, 0, readCount);
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
        return stringBuilder.toString();
    }

    public static void write(String string, File file, boolean isAppend) {
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

    public static void deleteDir(File file)
    {
        if (!file.isFile()) {
            File[] files = file.listFiles();
            if (files != null)
                for (File value : files)
                    deleteDir(value);
        }
        file.delete();
    }

    public static void copyFile(File sourceFile, File targetFile) {
        if (!sourceFile.isFile()) return;
        try {
            if (targetFile.exists())
                deleteDir(targetFile);
            Files.copy(sourceFile.toPath(), targetFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
