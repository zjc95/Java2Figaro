package trans.trace;

import org.eclipse.jdt.core.dom.*;
import trans.common.Util;

public class TraceParser {

    @SuppressWarnings("unchecked")
    public static String Analyze(String srcFile) {
        String source = Util.readFileToString(srcFile);
        ASTParser astParser = ASTParser.newParser(AST.JLS8);
        astParser.setSource(source.toCharArray());
        CompilationUnit srcUnit = (CompilationUnit) astParser.createAST(null);

        MethodVisitor methodVisitor = new MethodVisitor();
        methodVisitor.init(srcUnit);
        TraceUtil.init(srcUnit.getAST());
        //srcUnit.accept(methodVisitor);

        ImportDeclaration importDeclaration = TraceUtil.genImport();
        CompilationUnit targetUnit = (CompilationUnit) ASTNode.copySubtree(TraceUtil._ast, srcUnit);
        targetUnit.imports().add(importDeclaration);
        return targetUnit.toString();
    }
}
