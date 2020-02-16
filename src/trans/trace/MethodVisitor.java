package trans.trace;
import org.eclipse.jdt.core.dom.*;

public class MethodVisitor extends ASTVisitor {
    CompilationUnit _unit;

    MethodVisitor() {
    }

    void init(CompilationUnit unit) {
        _unit = unit;
    }

    public boolean visit(TypeDeclaration md) {
        for(MethodDeclaration methodDeclaration : md.getMethods()) {
            InstrumentationParser insParser = new InstrumentationParser(_unit);
            insParser.process(methodDeclaration);
        }
        return true;
    }
}
