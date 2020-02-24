package trans.trace;
import org.eclipse.jdt.core.dom.*;

public class MethodVisitor extends ASTVisitor {
    CompilationUnit _unit;
    String _methodName;

    MethodVisitor() {
    }

    void init(CompilationUnit unit, String methodName) {
        _unit = unit;
        _methodName = methodName;
    }

    public boolean visit(TypeDeclaration md) {
        for(MethodDeclaration methodDeclaration : md.getMethods())
            if (methodDeclaration.getName().getIdentifier().equals(_methodName)) {
                InstrumentationParser insParser = new InstrumentationParser(_unit);
                insParser.process(methodDeclaration);
            }
        return true;
    }
}
