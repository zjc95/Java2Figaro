package trans.trace;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import trans.common.Util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TraceParser {

    public static void runCmd(String strCmd) {
        try {
            Runtime.getRuntime().exec("cmd /c " + strCmd).waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static String Analyze(String srcFile) {
        CompilationUnit srcUnit = genASTFromSource(srcFile, null);
        if (srcUnit == null)
            return "";

        MethodVisitor methodVisitor = new MethodVisitor();
        methodVisitor.init(srcUnit);
        TraceUtil.init(srcUnit.getAST());
        srcUnit.accept(methodVisitor);

        ImportDeclaration importDeclaration = TraceUtil.genImport();
        srcUnit.imports().add(importDeclaration);

        return srcUnit.toString();
    }

    private static CompilationUnit genASTFromSource(String srcFile, String srcPath) {
        String source = Util.readFileToString(srcFile);
        if(source.isEmpty()) return null;

        ASTParser astParser = ASTParser.newParser(Util.JAVA_LEVEL);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(Util.JAVA_VERSION, options);
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
}
