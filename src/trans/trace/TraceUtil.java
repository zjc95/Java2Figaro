package trans.trace;

import org.eclipse.jdt.core.dom.*;
import trans.common.LevelLogger;
import trans.common.Util;

import java.util.ArrayList;
import java.util.List;

public class TraceUtil {
    static final int TRACE_TYPE_NONE = 0;
    static final int TRACE_TYPE_ASSIGN = 1;
    static final int TRACE_TYPE_STMT_BEGIN = 2;
    static final int TRACE_TYPE_STMT_END = 3;
    static final int TRACE_TYPE_ENTRY = 4;
    static final int TRACE_TYPE_RET = 5;
    static final int TRACE_TYPE_CONTROL = 6;

    static AST _ast = null;

    static void init(AST sourceAST) {
        _ast = sourceAST;
    }

    private static Expression genCastExpression(MethodInvocation expression, Type type) {
        if ((type == null) || (type instanceof WildcardType)) {
            LevelLogger.error("ERROR: Cannot Resolve Type Of " + expression.arguments().get(0).toString());
            return expression;
        }

        if (type.isPrimitiveType())
            return expression;

        CastExpression castExpression = _ast.newCastExpression();
        castExpression.setExpression(expression);
        castExpression.setType(type);
        return castExpression;
    }

    @SuppressWarnings("unchecked")
    static Expression genAssignExpression(Expression expression, Type type, int line, int column) {
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        methodInvocation.setExpression(_ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(_ast.newSimpleName("dump"));
        methodInvocation.arguments().add(expression);
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(TRACE_TYPE_ASSIGN)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(line)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(column)));
        return genCastExpression(methodInvocation, type);
    }

    static Statement genWriteStatement() {
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        methodInvocation.setExpression(_ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(_ast.newSimpleName("write"));
        return _ast.newExpressionStatement(methodInvocation);
    }

    @SuppressWarnings("unchecked")
    static Expression genReturnExpression(Expression expression, Type type, int line, int column) {
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        methodInvocation.setExpression(_ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(_ast.newSimpleName("dump"));
        methodInvocation.arguments().add(expression);
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(TRACE_TYPE_RET)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(line)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(column)));
        return genCastExpression(methodInvocation, type);
    }

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
    static Statement genControlStatementBegin(int line, int column) {
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        methodInvocation.setExpression(_ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(_ast.newSimpleName("dump"));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(TRACE_TYPE_STMT_BEGIN)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(line)));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(column)));
        return _ast.newExpressionStatement(methodInvocation);
    }

    @SuppressWarnings("unchecked")
    static Statement genControlStatementEnd(int line, int column) {
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        methodInvocation.setExpression(_ast.newName("trans.trace.Dumper"));
        methodInvocation.setName(_ast.newSimpleName("dump"));
        methodInvocation.arguments().add(_ast.newNumberLiteral(String.valueOf(TRACE_TYPE_STMT_END)));
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
    static Block genBlock(Statement stmtBegin, Statement stmtOld, Statement stmtEnd) {
        Block block = _ast.newBlock();
        block.statements().add(stmtBegin);
        if (stmtOld != null) {
            if (stmtOld instanceof Block) {
                Block blockOld = (Block) stmtOld;
                for (Object object : blockOld.statements()) {
                    ASTNode node = copyNode((ASTNode) object);
                    block.statements().add(node);
                    if ((node instanceof ReturnStatement) || (node instanceof BreakStatement) || (node instanceof ContinueStatement))
                        return block;
                }
            } else block.statements().add(stmtOld);
            if ((stmtOld instanceof ReturnStatement) || (stmtOld instanceof BreakStatement) || (stmtOld instanceof ContinueStatement))
                return block;
        }
        block.statements().add(stmtEnd);
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
    static Block genBlock(ArrayList<ASTNode> stmtListBegin, Block oldBlock, ArrayList<ASTNode> stmtListEnd) {
        Block block = _ast.newBlock();
        block.statements().addAll(stmtListBegin);
        for (Object object : oldBlock.statements()) {
            ASTNode node = copyNode((ASTNode) object);
            block.statements().add(node);
            if ((node instanceof ReturnStatement) || (node instanceof BreakStatement) || (node instanceof ContinueStatement))
                return block;
        }
        block.statements().addAll(stmtListEnd);
        return block;
    }

    static Type typeFromBinding(ITypeBinding typeBinding) {
        if (typeBinding == null) {
            return _ast.newWildcardType();
        }

        if (typeBinding.isPrimitive()) {
            return _ast.newPrimitiveType(
                    PrimitiveType.toCode(typeBinding.getName()));
        }

        if (typeBinding.isCapture()) {
            ITypeBinding wildCard = typeBinding.getWildcard();
            WildcardType capType = _ast.newWildcardType();
            ITypeBinding bound = wildCard.getBound();
            if (bound != null) {
                capType.setBound(typeFromBinding(bound),
                        wildCard.isUpperbound());
            }
            return capType;
        }

        if (typeBinding.isArray()) {
            Type elType = typeFromBinding(typeBinding.getElementType());
            return _ast.newArrayType(elType, typeBinding.getDimensions());
        }

        if (typeBinding.isParameterizedType()) {
            ParameterizedType traceType = _ast.newParameterizedType(
                    typeFromBinding(typeBinding.getErasure()));

            @SuppressWarnings("unchecked")
            List<Type> newTypeArgs = traceType.typeArguments();
            for (ITypeBinding typeArg : typeBinding.getTypeArguments()) {
                newTypeArgs.add(typeFromBinding(typeArg));
            }

            return traceType;
        }

        if (typeBinding.isWildcardType()) {
            WildcardType traceType = _ast.newWildcardType();
            if (typeBinding.getBound() != null) {
                traceType.setBound(typeFromBinding(typeBinding.getBound()));
            }
            return traceType;
        }

//        if(typeBinding.isGenericType()) {
//            System.out.println(typeBinding.toString());
//            return typeFromBinding(ast, typeBinding.getErasure());
//        }

        // simple or raw type
        String qualName = typeBinding.getName();
        if ("".equals(qualName)) {
            return _ast.newWildcardType();
        }
        try {
            return _ast.newSimpleType(_ast.newName(qualName));
        } catch (Exception e) {
            return _ast.newWildcardType();
        }
    }
}
