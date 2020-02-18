package trans.staticUtils;

import java.util.LinkedList;
import java.util.List;

import trans.common.LevelLogger;
import trans.common.Util;
import org.eclipse.jdt.core.dom.*;

public class StaticParser {
    public static StaticInfo Analyze(String srcFile) {
        String source = Util.readFileToString(srcFile);
        ASTParser astParser = ASTParser.newParser(Util.JAVA_LEVEL);
        astParser.setSource(source.toCharArray());
        CompilationUnit srcUnit = (CompilationUnit) astParser.createAST(null);

        MethodDeclCollector methodDeclCollector = new MethodDeclCollector();
        methodDeclCollector.init();
        srcUnit.accept(methodDeclCollector);
        List<MethodDeclaration> srcMethods = methodDeclCollector.getAllMethDecl();

        StaticInfo info = new StaticInfo();

        for (MethodDeclaration sm : srcMethods) {
            SourceParser parser = new SourceParser(srcUnit, info);
            parser.process(sm);
        }

        info.build();
        LevelLogger.debug(info.AnalyzeInformation());
        return info;
    }

    static class MethodDeclCollector extends ASTVisitor {

        List<MethodDeclaration> methodDeclarations;

        MethodDeclCollector() {
        }

        void init() {
            methodDeclarations = new LinkedList<>();
        }

        List<MethodDeclaration> getAllMethDecl() {
            return methodDeclarations;
        }

        public boolean visit(MethodDeclaration md) {
            methodDeclarations.add(md);
            return true;
        }
    }
}
