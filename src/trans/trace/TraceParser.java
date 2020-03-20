package trans.trace;

import org.eclipse.jdt.core.dom.*;
import trans.common.Util;

import java.io.File;
import java.io.IOException;

public class TraceParser {

    public static void runCmd(String strCmd) {
        try {
            Runtime.getRuntime().exec("cmd /c " + strCmd).waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static String analyze(File srcFile, String methodName) {
        CompilationUnit srcUnit = Util.genASTFromSource(srcFile, null);
        if (srcUnit == null)
            return "";

        MethodVisitor methodVisitor = new MethodVisitor();
        methodVisitor.init(srcUnit, methodName);
        TraceUtil.init(srcUnit.getAST());
        srcUnit.accept(methodVisitor);

        ImportDeclaration importDeclaration = TraceUtil.genImport();
        srcUnit.imports().add(importDeclaration);

        return srcUnit.toString();
    }
}
