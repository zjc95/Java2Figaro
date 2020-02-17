package trans.trace;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;

public class TraceUtil {
    static final int TRACE_TYPE_NONE = 0;
    static final int TRACE_TYPE_ASSIGN = 1;
    static final int TRACE_TYPE_STMT = 2;
    static final int TRACE_TYPE_ENTRY = 3;
    static final int TRACE_TYPE_RET = 4;
    static final int TRACE_TYPE_CONTROL = 5;

    static AST _ast = AST.newAST(AST.JLS8);

    @SuppressWarnings("unchecked")
    static Statement genEntryStatement(SimpleName var, int line, int column) {
        SimpleName simpleName = _ast.newSimpleName(var.getIdentifier());
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        methodInvocation.setExpression(_ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(_ast.newSimpleName("dump"));
        methodInvocation.arguments().add(simpleName);
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(TRACE_TYPE_ENTRY)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(line)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(column)));
        return _ast.newExpressionStatement(methodInvocation);
    }

    @SuppressWarnings("unchecked")
    static MethodInvocation genAssignExpression(Expression var, int line, int column) {
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        methodInvocation.setExpression(_ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(_ast.newSimpleName("dump"));
        methodInvocation.arguments().add(var);
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(TRACE_TYPE_ASSIGN)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(line)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(column)));
        return methodInvocation;
    }

    @SuppressWarnings("unchecked")
    static MethodInvocation genReturnExpression(Expression expr, int line, int column) {
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        methodInvocation.setExpression(_ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(_ast.newSimpleName("dump"));
        methodInvocation.arguments().add(expr);
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(TRACE_TYPE_RET)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(line)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(column)));
        return methodInvocation;
    }

    @SuppressWarnings("unchecked")
    static Statement genControlStatement(int line, int column) {
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        methodInvocation.setExpression(_ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(_ast.newSimpleName("dump"));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(TRACE_TYPE_STMT)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(line)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(column)));
        return _ast.newExpressionStatement(methodInvocation);
    }

    @SuppressWarnings("unchecked")
    static MethodInvocation genControlExpression(Expression var, int line, int column) {
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        methodInvocation.setExpression(_ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(_ast.newSimpleName("dump"));
        methodInvocation.arguments().add(var);
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(TRACE_TYPE_CONTROL)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(line)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(column)));
        return methodInvocation;
    }

    static ImportDeclaration genImport() {
        ImportDeclaration importDeclaration = _ast.newImportDeclaration();
        importDeclaration.setName(_ast.newName("trans.trace.Dumper"));
        return importDeclaration;
    }

    static ASTNode copyNode(ASTNode node) {
        return ASTNode.copySubtree(_ast, node);
    }

    static Block genBlock() {
        return _ast.newBlock();
    }

    @SuppressWarnings("unchecked")
    static Block genBlock(ASTNode stmt1, Statement stmt2) {
        Block block = _ast.newBlock();
        block.statements().add(stmt1);
        block.statements().add(stmt2);
        return block;
    }

    @SuppressWarnings("unchecked")
    static Block genBlock(ArrayList<ASTNode> stmtList, Block oldBlock) {
        Block block = _ast.newBlock();
        block.statements().addAll(stmtList);
        for (Object object : oldBlock.statements()) {
            ASTNode node = copyNode((ASTNode) object);
            block.statements().add(node);
        }
        return block;
    }

    @SuppressWarnings("unchecked")
    static Block genBlock(ASTNode stmt, Block oldBlock) {
        Block block = _ast.newBlock();
        block.statements().add(stmt);
        for (Object object : oldBlock.statements()) {
            ASTNode node = copyNode((ASTNode) object);
            block.statements().add(node);
        }
        return block;
    }
}
