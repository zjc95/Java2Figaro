package trans.trace;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class TraceUtil {
    static final int TRACE_TYPE_NONE = 0;
    static final int TRACE_TYPE_ASSIGN = 1;
    static final int TRACE_TYPE_STMT = 2;
    static final int TRACE_TYPE_ENTRY = 3;
    static final int TRACE_TYPE_RET = 4;
    static final int TRACE_TYPE_CONTROL = 5;

    private static AST ast = AST.newAST(AST.JLS8);

    @SuppressWarnings("unchecked")
    public static Statement genEntryStatement(ASTNode var, int line, int column) {
        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(ast.newSimpleName("dump"));
        methodInvocation.arguments().add(var);
        methodInvocation.arguments().add(TRACE_TYPE_ENTRY);
        methodInvocation.arguments().add(line);
        methodInvocation.arguments().add(column);
        return ast.newExpressionStatement(methodInvocation);
    }

    @SuppressWarnings("unchecked")
    public static Statement genControlStatement(ASTNode var, int line, int column) {
        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(ast.newSimpleName("dump"));
        methodInvocation.arguments().add(TRACE_TYPE_STMT);
        methodInvocation.arguments().add(line);
        methodInvocation.arguments().add(column);
        return ast.newExpressionStatement(methodInvocation);
    }

    @SuppressWarnings("unchecked")
    public static Expression genAssignExpression(ASTNode var, int line, int column) {
        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(ast.newSimpleName("dump"));
        methodInvocation.arguments().add(var);
        methodInvocation.arguments().add(TRACE_TYPE_ASSIGN);
        methodInvocation.arguments().add(line);
        methodInvocation.arguments().add(column);
        return methodInvocation;
    }

    @SuppressWarnings("unchecked")
    public static Expression genControlExpression(ASTNode var, int line, int column) {
        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(ast.newSimpleName("dump"));
        methodInvocation.arguments().add(var);
        methodInvocation.arguments().add(TRACE_TYPE_CONTROL);
        methodInvocation.arguments().add(line);
        methodInvocation.arguments().add(column);
        return methodInvocation;
    }

    @SuppressWarnings("unchecked")
    public static MethodInvocation genReturnExpression(ASTNode expr, int line, int column) {
        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(ast.newSimpleName("dump"));
        methodInvocation.arguments().add(expr);
        methodInvocation.arguments().add(TRACE_TYPE_RET);
        methodInvocation.arguments().add(line);
        methodInvocation.arguments().add(column);
        return methodInvocation;
    }

    @SuppressWarnings("unchecked")
    public static Block genBlock(ArrayList<ASTNode> stmtList1, Block oldBlock) {
        Block block = ast.newBlock();
        block.statements().addAll(stmtList1);
        block.statements().addAll(oldBlock.statements());
        return block;
    }

    @SuppressWarnings("unchecked")
    public static Block genBlock(ASTNode stmt, Block oldBlock) {
        Block block = ast.newBlock();
        block.statements().add(stmt);
        block.statements().addAll(oldBlock.statements());
        return block;
    }
}
