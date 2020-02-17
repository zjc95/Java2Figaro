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

        CompilationUnit targetUnit = (CompilationUnit) TraceUtil.copyNode(srcUnit);
        targetUnit.accept(methodVisitor);

        ImportDeclaration importDeclaration = TraceUtil.genImport();
        targetUnit.imports().add(importDeclaration);

        return targetUnit.toString();
    }
}
