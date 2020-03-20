package trans.staticUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import trans.common.Util;
import org.eclipse.jdt.core.dom.*;

public class StaticParser {
    public static StaticInfo analyze(File srcFile) {
        return analyze(srcFile, null);
    }

    public static StaticInfo analyze(File srcFile, String methodName) {
        CompilationUnit srcUnit = Util.genASTFromSource(srcFile, null);
        if (srcUnit == null)
            return null;

        MethodDeclarationCollector methodDeclarationCollector = new MethodDeclarationCollector();
        methodDeclarationCollector.init();
        srcUnit.accept(methodDeclarationCollector);
        List<MethodDeclaration> srcMethods = methodDeclarationCollector.getAllMethodDeclarations();

        StaticInfo info = new StaticInfo();

        for (MethodDeclaration sm : srcMethods)
            if ((methodName == null) || (sm.getName().getIdentifier().equals(methodName))) {
                SourceParser parser = new SourceParser(srcUnit, info);
                parser.process(sm);
            }

        info.build();
        //LevelLogger.debug(info.AnalyzeInformation());
        return info;
    }

    static class MethodDeclarationCollector extends ASTVisitor {

        List<MethodDeclaration> methodDeclarations;

        MethodDeclarationCollector() {
        }

        void init() {
            methodDeclarations = new LinkedList<>();
        }

        List<MethodDeclaration> getAllMethodDeclarations() {
            return methodDeclarations;
        }

        public boolean visit(MethodDeclaration md) {
            methodDeclarations.add(md);
            return true;
        }
    }
}
