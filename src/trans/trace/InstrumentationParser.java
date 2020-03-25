package trans.trace;

import org.eclipse.jdt.core.dom.*;
import trans.common.LevelLogger;

import java.util.ArrayList;

public class InstrumentationParser {
    private CompilationUnit _unit;
    private Statement _tryCtrlStmtBegin;
    private Statement _tryCtrlStmtEnd;
    private Type _retType = null;
    private Type _declType = null;

    /************************** Visit MethodDeclaration ***********************/

    @SuppressWarnings("unchecked")
    private ASTNode visit(MethodDeclaration node, int traceType) {
        if(node.getReturnType2() != null)
            _retType = TraceUtil.typeFromBinding(node.getReturnType2().resolveBinding());

        ArrayList<ASTNode> params = new ArrayList<>();
        for (Object arg : node.parameters())
            params.add(process((ASTNode) arg, TraceUtil.TRACE_TYPE_ENTRY));

        Block body = node.getBody();
        if (body != null) {
            Block newBody = (Block) process(body, traceType);
            node.setBody(TraceUtil.genBlock(params, newBody));
        }

        if (node.getReturnType2().toString().equals("void")) {
            node.getBody().statements().add(TraceUtil.genWriteStatement());
        }
        return node;
    }

    /****************************** Visit BLOCK *******************************/

    /**
     * Block:
     * { { Statement } }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(Block node, int traceType) {
        Block block = TraceUtil.genBlock();
        for (Object object : node.statements()) {
            Statement astNode = (Statement) process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE);
            block.statements().add(astNode);
        }
        return block;
    }

    /*************************** Visit Statement ******************************/

    /**
     * ConstructorInvocation:
     * [ < Type { , Type } > ]
     * this ( [ Expression { , Expression } ] ) ;
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ConstructorInvocation node, int traceType) {
        ConstructorInvocation myNode = (ConstructorInvocation) TraceUtil.copyNode(node);

        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
        myNode.arguments().clear();
        myNode.arguments().addAll(arguments);

        return myNode;
    }

    /**
     * SuperConstructorInvocation:
     *	[ Expression . ]
     *	    [ < Type { , Type } > ]
     *	    super ( [ Expression { , Expression } ] ) ;
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(SuperConstructorInvocation node, int traceType) {
        SuperConstructorInvocation myNode = (SuperConstructorInvocation) TraceUtil.copyNode(node);

        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
        myNode.arguments().clear();
        myNode.arguments().addAll(arguments);

        return myNode;
    }

    /**
     * AssertStatement:
     *	assert Expression [ : Expression ] ;
     */
    private ASTNode visit(AssertStatement node, int traceType) {
        AssertStatement myNode = (AssertStatement) TraceUtil.copyNode(node);
        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return myNode;
    }

    /**
     * BreakStatement:
     *	break [ Identifier ] ;
     */
    private ASTNode visit(BreakStatement node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ContinueStatement:
     * continue [ Identifier ] ;
     */
    private ASTNode visit(ContinueStatement node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * EmptyStatement:
     *	;
     */
    private ASTNode visit(EmptyStatement node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ExpressionStatement:
     * StatementExpression ;
     */
    private ASTNode visit(ExpressionStatement node, int traceType) {
        ExpressionStatement myNode = (ExpressionStatement) TraceUtil.copyNode(node);
        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return myNode;
    }

    /**
     * LabeledStatement:
     *	Identifier : Statement
     */
    private ASTNode visit(LabeledStatement node, int traceType) {
        LabeledStatement myNode = (LabeledStatement) TraceUtil.copyNode(node);
        if (node.getBody() != null)
            myNode.setBody((Statement) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE));
        return myNode;
    }

    /**
     * ReturnStatement:
     *	return [ Expression ] ;
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ReturnStatement node, int traceType) {
        ReturnStatement myNode = (ReturnStatement) TraceUtil.copyNode(node);
        if (node.getExpression() != null)
        {
            Expression expression = (Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE);
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            Type type = TraceUtil.typeFromBinding(node.getExpression().resolveTypeBinding());
            if ((type == null) || (type instanceof WildcardType)) type = _retType;
            myNode.setExpression(TraceUtil.genReturnExpression(expression, (Type) TraceUtil.copyNode(type), line, column));
            return myNode;
        }
        else {
            Statement writeStatement = TraceUtil.genWriteStatement();
            Block block = TraceUtil.genBlock();
            block.statements().add(writeStatement);
            block.statements().add(myNode);
            return block;
        }
    }

    /**
     * SynchronizedStatement:
     *	synchronized ( Expression ) Block
     */
    private ASTNode visit(SynchronizedStatement node, int traceType) {
        SynchronizedStatement myNode = (SynchronizedStatement) TraceUtil.copyNode(node);

        if (node.getExpression() != null)
            myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement controlStatementBegin = TraceUtil.genControlStatementBegin(line, column);
        Statement controlStatementEnd = TraceUtil.genControlStatementEnd(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        myNode.setBody(TraceUtil.genBlock(controlStatementBegin, block, controlStatementEnd));

        return myNode;
    }

    /**
     * TypeDeclarationStatement:
     *	TypeDeclaration
     *	EnumDeclaration
     */
    private ASTNode visit(TypeDeclarationStatement node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * VariableDeclarationStatement: { ExtendedModifier } Type
     * VariableDeclarationFragment { , VariableDeclarationFragment } ;
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(VariableDeclarationStatement node, int traceType) {
        VariableDeclarationStatement myNode = (VariableDeclarationStatement) TraceUtil.copyNode(node);

        ArrayList<ASTNode> fragments = new ArrayList<>();
        for (Object object : node.fragments())
            fragments.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
        myNode.fragments().clear();
        myNode.fragments().addAll(fragments);

        return myNode;
    }

    //-------------------Branch------------------------
    /**
     * IfStatement:
     *	if ( Expression ) Statement [ else Statement]
     */
    private ASTNode visit(IfStatement node, int traceType) {
        IfStatement myNode = (IfStatement) TraceUtil.copyNode(node);

        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement thenStatementControlBegin = TraceUtil.genControlStatementBegin(line, column);
        Statement thenStatementControlEnd = TraceUtil.genControlStatementEnd(line, column);
        Statement thenStatement = (Statement) process(node.getThenStatement(), traceType);
        myNode.setThenStatement(TraceUtil.genBlock(thenStatementControlBegin, thenStatement, thenStatementControlEnd));

        Statement elseStatementControlBegin = TraceUtil.genControlStatementBegin(line, column);
        Statement elseStatementControlEnd = TraceUtil.genControlStatementEnd(line, column);
        if (node.getElseStatement() != null) {
            Statement elseStatement = (Statement) process(node.getElseStatement(), traceType);
            myNode.setElseStatement(TraceUtil.genBlock(elseStatementControlBegin, elseStatement, elseStatementControlEnd));
        }
        else TraceUtil.genBlock(elseStatementControlBegin, null, elseStatementControlEnd);

        return myNode;
    }

    /**
     * SwitchCase:
     *           case Expression  :
     *           default :
     */
    private ASTNode visit(SwitchCase node, int traceType) {
        SwitchCase myNode = (SwitchCase) TraceUtil.copyNode(node);

        if (node.getExpression() != null)
            myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return myNode;
    }

    /**
     * SwitchStatement:
     *           switch ( Expression )
     *                   { { SwitchCase | Statement } }
     * SwitchCase:
     *           case Expression  :
     *           default :
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(SwitchStatement node, int traceType) {
        SwitchStatement myNode = (SwitchStatement) TraceUtil.copyNode(node);

        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        ArrayList<ASTNode> statements = new ArrayList<>();
        for (Object object : node.statements())
            if (!(object instanceof SwitchCase)) {
                Statement controlStatementBegin = TraceUtil.genControlStatementBegin(line, column);
                Statement controlStatementEnd = TraceUtil.genControlStatementEnd(line, column);
                Statement statement = (Statement) process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE);
                statements.add(TraceUtil.genBlock(controlStatementBegin, statement, controlStatementEnd));
            }
        myNode.statements().clear();
        myNode.statements().addAll(statements);

        return myNode;
    }

    //--------------------Loop-------------------------
    /**
     * EnhancedForStatement:
     * for ( FormalParameter : Expression )
     * Statement
     */
    private ASTNode visit(EnhancedForStatement node, int traceType) {
        EnhancedForStatement myNode = (EnhancedForStatement) TraceUtil.copyNode(node);

        myNode.setParameter((SingleVariableDeclaration) process(node.getParameter(), TraceUtil.TRACE_TYPE_NONE));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Expression expression = (Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE);
        Type type = TraceUtil.typeFromBinding(node.getExpression().resolveTypeBinding());
        myNode.setExpression(TraceUtil.genAssignExpression(expression, (Type) TraceUtil.copyNode(type), line, column));

        Statement controlStatementBegin = TraceUtil.genControlStatementBegin(line, column);
        Statement controlStatementEnd = TraceUtil.genControlStatementEnd(line, column);
        Statement statement = (Statement) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        myNode.setBody(TraceUtil.genBlock(controlStatementBegin, statement, controlStatementEnd));

        return myNode;
    }

    /**
     * for (
     *      [ ForInit ];
     *      [ Expression ] ;
     *      [ ForUpdate ] )
     *      Statement
     * ForInit:
     *      Expression { , Expression }
     * ForUpdate:
     *      Expression { , Expression }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ForStatement node, int traceType) {
        ForStatement myNode = (ForStatement) TraceUtil.copyNode(node);

        if (!node.initializers().isEmpty()) {
            ArrayList<ASTNode> initializers = new ArrayList<>();
            for (Object object : node.initializers())
                initializers.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
            myNode.initializers().clear();
            myNode.initializers().addAll(initializers);
        }

        if (node.getExpression() != null)
            myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        if (!node.updaters().isEmpty()) {
            ArrayList<ASTNode> updaters = new ArrayList<>();
            for (Object object : node.updaters())
                updaters.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
            myNode.initializers().clear();
            myNode.initializers().addAll(updaters);
        }

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement controlStatementBegin = TraceUtil.genControlStatementBegin(line, column);
        Statement controlStatementEnd = TraceUtil.genControlStatementEnd(line, column);
        Statement statement = (Statement) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        if (statement instanceof Block) {
            Block block = (Block) statement;
            myNode.setBody(TraceUtil.genBlock(controlStatementBegin, block, controlStatementEnd));
        }
        else myNode.setBody(TraceUtil.genBlock(controlStatementBegin, statement, controlStatementEnd));

        return myNode;
    }

    /**
     * DoStatement:
     *	do Statement while ( Expression ) ;
     */
    private ASTNode visit(DoStatement node, int traceType) {
        DoStatement myNode = (DoStatement) TraceUtil.copyNode(node);

        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement controlStatementBegin = TraceUtil.genControlStatementBegin(line, column);
        Statement controlStatementEnd = TraceUtil.genControlStatementEnd(line, column);
        Statement statement = (Statement) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        myNode.setBody(TraceUtil.genBlock(controlStatementBegin, statement, controlStatementEnd));

        return myNode;
    }

    /**
     * WhileStatement:
     *	while ( Expression ) Statement
     */
    private ASTNode visit(WhileStatement node, int traceType) {
        WhileStatement myNode = (WhileStatement) TraceUtil.copyNode(node);

        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement controlStatementBegin = TraceUtil.genControlStatementBegin(line, column);
        Statement controlStatementEnd = TraceUtil.genControlStatementEnd(line, column);
        Statement statement = (Statement) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        myNode.setBody(TraceUtil.genBlock(controlStatementBegin, statement, controlStatementEnd));

        return myNode;
    }

    //--------------------Try-Catch-------------------
    /**
     * TryStatement:
     *	try [ ( Resources ) ]
     *	    Block
     *	    [ { CatchClause } ]
     *	    [ finally Block ]
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(TryStatement node, int traceType) {
        TryStatement myNode = (TryStatement) TraceUtil.copyNode(node);

        if (node.resources() != null) {
            ArrayList<ASTNode> resources = new ArrayList<>();
            for (Object object : node.resources())
                resources.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
            myNode.resources().clear();
            myNode.resources().addAll(resources);
        }

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        _tryCtrlStmtBegin = TraceUtil.genControlStatementBegin(line, column);
        _tryCtrlStmtEnd = TraceUtil.genControlStatementBegin(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        myNode.setBody(TraceUtil.genBlock(_tryCtrlStmtBegin, block, _tryCtrlStmtEnd));

        ArrayList<ASTNode> catchClauses = new ArrayList<>();
        for (Object object : node.catchClauses()) {
            CatchClause catchClause = (CatchClause) object;
            catchClauses.add(process(catchClause, TraceUtil.TRACE_TYPE_NONE));
        }
        myNode.catchClauses().clear();
        myNode.catchClauses().addAll(catchClauses);

        if (node.getFinally() != null) {
            Statement controlStatementBegin = TraceUtil.genControlStatementBegin(line, column);
            Statement controlStatementEnd = TraceUtil.genControlStatementEnd(line, column);
            Block finallyBlock = (Block) process(node.getFinally(), TraceUtil.TRACE_TYPE_NONE);
            myNode.setFinally(TraceUtil.genBlock(controlStatementBegin, finallyBlock, controlStatementEnd));
        }

        return myNode;
    }

    /**
     * CatchClause
     *    catch ( SingleVariableDeclaration ) Block
     */
    private ASTNode visit(CatchClause node, int traceType) {
        CatchClause myNode = (CatchClause) TraceUtil.copyNode(node);

        Statement entryStatement = (Statement) process(node.getException(), TraceUtil.TRACE_TYPE_ENTRY);

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement controlStatementBegin = TraceUtil.genControlStatementBegin(line, column);
        Statement controlStatementEnd = TraceUtil.genControlStatementEnd(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);

        ArrayList<ASTNode> statementsBegin = new ArrayList<>();
        ArrayList<ASTNode> statementsEnd = new ArrayList<>();
        statementsBegin.add(TraceUtil.copyNode(_tryCtrlStmtBegin));
        statementsBegin.add(controlStatementBegin);
        statementsBegin.add(entryStatement);
        statementsEnd.add(controlStatementEnd);
        statementsEnd.add(_tryCtrlStmtEnd);
        myNode.setBody(TraceUtil.genBlock(statementsBegin, block, statementsEnd));

        return myNode;
    }

    /**
     * ThrowStatement:
     *	throw Expression ;
     */
    private ASTNode visit(ThrowStatement node, int traceType) {
        ThrowStatement myNode = (ThrowStatement) TraceUtil.copyNode(node);
        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return myNode;
    }

    /************************** Visit Expression ******************************/
    private Expression controlExpressionNode(Expression node, int traceType) {
        if (traceType == TraceUtil.TRACE_TYPE_CONTROL) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    private int checkType(int traceType) {
        if (traceType == TraceUtil.TRACE_TYPE_CONTROL)
            return TraceUtil.TRACE_TYPE_NONE;
        return traceType;
    }

    /**
     * Annotation:
     * NormalAnnotation
     * MarkerAnnotation
     * SingleMemberAnnotation
     */
    private ASTNode visit(Annotation node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ArrayAccess:
     * Expression [ Expression ]
     */
    private ASTNode visit(ArrayAccess node, int traceType) {
        ArrayAccess myNode = (ArrayAccess) TraceUtil.copyNode(node);
        myNode.setIndex((Expression) process(node.getIndex(), checkType(traceType)));
        return controlExpressionNode(myNode, traceType);
    }

    /**
     * ArrayCreation: new PrimitiveType [ Expression ] { [ Expression ] } { [ ]
     * } new TypeName [ < Type { , Type } > ] [ Expression ] { [ Expression ] }
     * { [ ] } new PrimitiveType [ ] { [ ] } ArrayInitializer new TypeName [ <
     * Type { , Type } > ] [ ] { [ ] } ArrayInitializer
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ArrayCreation node, int traceType) {
        ArrayCreation myNode = (ArrayCreation) TraceUtil.copyNode(node);

        ArrayList<ASTNode> dimensions = new ArrayList<>();
        for (Object object : node.dimensions())
            dimensions.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
        myNode.dimensions().clear();
        myNode.dimensions().addAll(dimensions);

        if (node.getInitializer() != null)
            myNode.setInitializer((ArrayInitializer) process(node.getInitializer(), traceType));

        return myNode;
    }

    /**
     * ArrayInitializer:
     *      { [ Expression { , Expression} [ , ]] }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ArrayInitializer node, int traceType) {
        ArrayInitializer myNode = (ArrayInitializer) TraceUtil.copyNode(node);

        ArrayList<ASTNode> expressions = new ArrayList<>();
        for (Object object : node.expressions())
            expressions.add(process((ASTNode) object, traceType));
        myNode.expressions().clear();
        myNode.expressions().addAll(expressions);

        return myNode;
    }

    /**
     * CastExpression:
     * ( Type ) Expression
     */
    private ASTNode visit(CastExpression node, int traceType) {
        CastExpression myNode = (CastExpression) TraceUtil.copyNode(node);
        node.setExpression((Expression) process(node.getExpression(), checkType(traceType)));
        return controlExpressionNode(myNode, traceType);
    }

    /**
     * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type
     * ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ClassInstanceCreation node, int traceType) {
        ClassInstanceCreation myNode = (ClassInstanceCreation) TraceUtil.copyNode(node);

        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, traceType));
        myNode.arguments().clear();
        myNode.arguments().addAll(arguments);

        return myNode;
    }

    private ASTNode visit(AnonymousClassDeclaration node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ConditionalExpression:
     *      Expression ? Expression : Expression
     */
    private ASTNode visit(ConditionalExpression node, int traceType) {
        ConditionalExpression myNode = (ConditionalExpression) TraceUtil.copyNode(node);
        int typeNow = checkType(traceType);

        myNode.setExpression((Expression) process(node.getExpression(), typeNow));
        myNode.setThenExpression((Expression) process(node.getThenExpression(), typeNow));
        myNode.setElseExpression((Expression) process(node.getElseExpression(), typeNow));

        return controlExpressionNode(myNode, traceType);
    }

    /**
     * InfixExpression:
     *		Expression InfixOperator Expression { InfixOperator Expression }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(InfixExpression node, int traceType) {
        InfixExpression myNode = (InfixExpression) TraceUtil.copyNode(node);

        int typeNow = traceType;
        if ((node.getOperator() != InfixExpression.Operator.CONDITIONAL_AND) &&
            (node.getOperator() != InfixExpression.Operator.CONDITIONAL_OR))
            typeNow = checkType(traceType);

        myNode.setLeftOperand((Expression) process(node.getLeftOperand(), typeNow));
        myNode.setRightOperand((Expression) process(node.getRightOperand(), typeNow));

        if (node.hasExtendedOperands()) {
            ArrayList<ASTNode> extendedOperands = new ArrayList<>();
            for (Object object : node.extendedOperands())
                extendedOperands.add(process((Expression) object, typeNow));
            myNode.extendedOperands().clear();
            myNode.extendedOperands().addAll(extendedOperands);
        }

        if (typeNow != traceType)
            return controlExpressionNode(myNode, traceType);
        else
            return myNode;
    }

    /**
     * InstanceofExpression:
     *		Expression instanceof Type
     */
    private ASTNode visit(InstanceofExpression node, int traceType) {
        InstanceofExpression myNode = (InstanceofExpression) TraceUtil.copyNode(node);
        myNode.setLeftOperand((Expression) process(node.getLeftOperand(), checkType(traceType)));
        return controlExpressionNode(myNode, traceType);
    }

    /**
     * LambdaExpression:
     *	Identifier -> Body
     *	( [ Identifier { , Identifier } ] ) -> Body
     *	( [ FormalParameter { , FormalParameter } ] ) -> Body
     */
    private ASTNode visit(LambdaExpression node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     *  MethodInvocation:
     *  [ Expression . ]
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(MethodInvocation node, int traceType) {
        MethodInvocation myNode = (MethodInvocation) TraceUtil.copyNode(node);

        myNode.setExpression((Expression) process(node.getExpression(), checkType(traceType)));
        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, checkType(traceType)));
        myNode.arguments().clear();
        myNode.arguments().addAll(arguments);

        return controlExpressionNode(myNode, traceType);
    }

    /**
     * ParenthesizedExpression:
     *	( Expression )
     */
    private ASTNode visit(ParenthesizedExpression node, int traceType) {
        ParenthesizedExpression myNode = (ParenthesizedExpression) TraceUtil.copyNode(node);
        myNode.setExpression((Expression) process(node.getExpression(), traceType));
        return myNode;
    }

    /**
     * PostfixExpression:
     *	Expression PostfixOperator
     */
    private ASTNode visit(PostfixExpression node, int traceType) {
        PostfixExpression myNode = (PostfixExpression) TraceUtil.copyNode(node);
        myNode.setOperand((Expression) process(node.getOperand(), checkType(traceType)));
        return controlExpressionNode(myNode, traceType);
    }

    /**
     * PrefixExpression:
     *	PrefixOperator Expression
     */
    private ASTNode visit(PrefixExpression node, int traceType) {
        PrefixExpression myNode = (PrefixExpression) TraceUtil.copyNode(node);
        myNode.setOperand((Expression) process(node.getOperand(), checkType(traceType)));
        return controlExpressionNode(myNode, traceType);
    }

    /**
     * SuperMethodInvocation:
     *	[ ClassName . ] super .
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(SuperMethodInvocation node, int traceType) {
        SuperMethodInvocation myNode = (SuperMethodInvocation) TraceUtil.copyNode(node);

        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, checkType(traceType)));
        myNode.arguments().clear();
        myNode.arguments().addAll(arguments);

        return controlExpressionNode(myNode, traceType);
    }

    /**
     * Assignment:
     *      Expression AssignmentOperator Expression
     */
    private ASTNode visit(Assignment node, int traceType) {
        Assignment myNode = (Assignment) TraceUtil.copyNode(node);

        myNode.setLeftHandSide((Expression) process(node.getLeftHandSide(), checkType(traceType)));

        Expression expression = (Expression) process(node.getRightHandSide(), checkType(traceType));
        Type type = TraceUtil.typeFromBinding(node.getRightHandSide().resolveTypeBinding());
        if ((type == null) || (type instanceof WildcardType))
            type = TraceUtil.typeFromBinding(node.getLeftHandSide().resolveTypeBinding());
        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        myNode.setRightHandSide(TraceUtil.genAssignExpression(expression, (Type) TraceUtil.copyNode(type), line, column));

        return controlExpressionNode(myNode, traceType);
    }

    //-----------------------Declaration---------------------------
    /**
     * { ExtendedModifier } Type {Annotation} [ ... ] Identifier { Dimension } [ = Expression ]
     * "..." should not be appear since it is only used in method declarations
     */
    private ASTNode visit(SingleVariableDeclaration node, int traceType) {
        SingleVariableDeclaration myNode = (SingleVariableDeclaration) TraceUtil.copyNode(node);

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        if (traceType == TraceUtil.TRACE_TYPE_ENTRY)
            return TraceUtil.genEntryStatement(node.getName(), line, column);

        if (node.getInitializer() != null) {
            Expression expression = (Expression) process(node.getInitializer(), traceType);
            Type type = TraceUtil.typeFromBinding(node.getInitializer().resolveTypeBinding());
            if ((type == null) || (type instanceof WildcardType))
                type = node.getType();
            myNode.setInitializer(TraceUtil.genAssignExpression(expression, (Type) TraceUtil.copyNode(type), line, column));
        }

        return myNode;
    }

    /**
     * VariableDeclarationExpression:
     *	{ ExtendedModifier } Type VariableDeclarationFragment
     *	    { , VariableDeclarationFragment }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(VariableDeclarationExpression node, int traceType) {
        VariableDeclarationExpression myNode = (VariableDeclarationExpression) TraceUtil.copyNode(node);
        _declType = node.getType();

        ArrayList<ASTNode> fragments = new ArrayList<>();
        for (Object object : node.fragments())
            fragments.add(process((ASTNode) object, traceType));
        myNode.fragments().clear();
        myNode.fragments().addAll(fragments);

        return myNode;
    }

    /**
     * VariableDeclarationFragment:
     *	Identifier { Dimension } [ = Expression ]
     */
    private ASTNode visit(VariableDeclarationFragment node, int traceType) {
        VariableDeclarationFragment myNode = (VariableDeclarationFragment) TraceUtil.copyNode(node);

        if (node.getInitializer() != null){
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            Expression expression = (Expression) process(node.getInitializer(), traceType);
            Type type = TraceUtil.typeFromBinding(node.getInitializer().resolveTypeBinding());
            if ((type == null) || (type instanceof WildcardType))
                type = _declType;
            myNode.setInitializer(TraceUtil.genAssignExpression(expression, (Type) TraceUtil.copyNode(type), line, column));
        }

        return myNode;
    }

    //---------------------------Variable----------------------------
    /**
     * FieldAccess:
     *           Expression . Identifier
     */
    private ASTNode visit(FieldAccess node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * Name:
     *	SimpleName
     *	QualifiedName
     */
    private ASTNode visit(Name node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * SuperFieldAccess:
     *	[ ClassName . ] super . Identifier
     */
    private ASTNode visit(SuperFieldAccess node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    //-------------------------Literal--------------------------
    /**
     * BooleanLiteral:
     *      true false
     */
    private ASTNode visit(BooleanLiteral node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * Character literal nodes.
     */
    private ASTNode visit(CharacterLiteral node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * Null literal node.
     */
    private ASTNode visit(NullLiteral node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * Number literal node.
     */
    private ASTNode visit(NumberLiteral node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * String literal nodes.
     */
    private ASTNode visit(StringLiteral node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * TypeLiteral:
     *	( Type | void ) . class
     */
    private ASTNode visit(TypeLiteral node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ThisExpression:
     *	[ ClassName . ] this
     */
    private ASTNode visit(ThisExpression node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    //-------------------------Reference-------------------
    /**
     * CreationReference:
     *      Type ::
     *          [ < Type { , Type } > ]
     *      new
     */
    private ASTNode visit(CreationReference node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ExpressionMethodReference:
     *	Expression ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private ASTNode visit(ExpressionMethodReference node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * MethodReference:
     *	CreationReference
     *	ExpressionMethodReference
     *	SuperMethodReference
     *	TypeMethodReference
     */
    private ASTNode visit(MethodReference node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * SuperMethodReference:
     *	[ ClassName . ] super ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private ASTNode visit(SuperMethodReference node, int traceType) {
        return TraceUtil.copyNode(node);
    }

    /**
     * TypeMethodReference:
     *	Type ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private ASTNode visit(TypeMethodReference node, int traceType) {
        return TraceUtil.copyNode(node);
    }


    /************************** Process ******************************/
    InstrumentationParser(CompilationUnit unit) {
        _unit = unit;
    }

    ASTNode process(ASTNode node) {
        return process(node, TraceUtil.TRACE_TYPE_NONE);
    }

    private ASTNode process(ASTNode node, int traceType) {
        if (node == null) {
            return null;
        }
        if (node instanceof AssertStatement) {
            return visit((AssertStatement) node, traceType);
        } else if (node instanceof Block) {
            return visit((Block) node, traceType);
        } else if (node instanceof BreakStatement) {
            return visit((BreakStatement) node, traceType);
        } else if (node instanceof ConstructorInvocation) {
            return visit((ConstructorInvocation) node, traceType);
        } else if (node instanceof ContinueStatement) {
            return visit((ContinueStatement) node, traceType);
        } else if (node instanceof DoStatement) {
            return visit((DoStatement) node, traceType);
        } else if (node instanceof EmptyStatement) {
            return visit((EmptyStatement) node, traceType);
        } else if (node instanceof EnhancedForStatement) {
            return visit((EnhancedForStatement) node, traceType);
        } else if (node instanceof ExpressionStatement) {
            return visit((ExpressionStatement) node, traceType);
        } else if (node instanceof ForStatement) {
            return visit((ForStatement) node, traceType);
        } else if (node instanceof IfStatement) {
            return visit((IfStatement) node, traceType);
        } else if (node instanceof LabeledStatement) {
            return visit((LabeledStatement) node, traceType);
        } else if (node instanceof ReturnStatement) {
            return visit((ReturnStatement) node, traceType);
        } else if (node instanceof SuperConstructorInvocation) {
            return visit((SuperConstructorInvocation) node, traceType);
        } else if (node instanceof SwitchCase) {
            return visit((SwitchCase) node, traceType);
        } else if (node instanceof SwitchStatement) {
            return visit((SwitchStatement) node, traceType);
        } else if (node instanceof SynchronizedStatement) {
            return visit((SynchronizedStatement) node, traceType);
        } else if (node instanceof ThrowStatement) {
            return visit((ThrowStatement) node, traceType);
        } else if (node instanceof TryStatement) {
            return visit((TryStatement) node, traceType);
        } else if (node instanceof TypeDeclarationStatement) {
            return visit((TypeDeclarationStatement) node, traceType);
        } else if (node instanceof VariableDeclarationStatement) {
            return visit((VariableDeclarationStatement) node, traceType);
        } else if (node instanceof WhileStatement) {
            return visit((WhileStatement) node, traceType);
        } else if (node instanceof Annotation) {
            return visit((Annotation) node, traceType);
        } else if (node instanceof ArrayAccess) {
            return visit((ArrayAccess) node, traceType);
        } else if (node instanceof ArrayCreation) {
            return visit((ArrayCreation) node, traceType);
        } else if (node instanceof ArrayInitializer) {
            return visit((ArrayInitializer) node, traceType);
        } else if (node instanceof Assignment) {
            return visit((Assignment) node, traceType);
        } else if (node instanceof BooleanLiteral) {
            return visit((BooleanLiteral) node, traceType);
        } else if (node instanceof CastExpression) {
            return visit((CastExpression) node, traceType);
        } else if (node instanceof CharacterLiteral) {
            return visit((CharacterLiteral) node, traceType);
        } else if (node instanceof ClassInstanceCreation) {
            return visit((ClassInstanceCreation) node, traceType);
        } else if (node instanceof ConditionalExpression) {
            return visit((ConditionalExpression) node, traceType);
        } else if (node instanceof CreationReference) {
            return visit((CreationReference) node, traceType);
        } else if (node instanceof ExpressionMethodReference) {
            return visit((ExpressionMethodReference) node, traceType);
        } else if (node instanceof FieldAccess) {
            return visit((FieldAccess) node, traceType);
        } else if (node instanceof InfixExpression) {
            return visit((InfixExpression) node, traceType);
        } else if (node instanceof InstanceofExpression) {
            return visit((InstanceofExpression) node, traceType);
        } else if (node instanceof LambdaExpression) {
            return visit((LambdaExpression) node, traceType);
        } else if (node instanceof MethodInvocation) {
            return visit((MethodInvocation) node, traceType);
        } else if (node instanceof MethodReference) {
            return visit((MethodReference) node, traceType);
        } else if (node instanceof Name) {
            return visit((Name) node, traceType);
        } else if (node instanceof NullLiteral) {
            return visit((NullLiteral) node, traceType);
        } else if (node instanceof NumberLiteral) {
            return visit((NumberLiteral) node, traceType);
        } else if (node instanceof ParenthesizedExpression) {
            return visit((ParenthesizedExpression) node, traceType);
        } else if (node instanceof PostfixExpression) {
            return visit((PostfixExpression) node, traceType);
        } else if (node instanceof PrefixExpression) {
            return visit((PrefixExpression) node, traceType);
        } else if (node instanceof StringLiteral) {
            return visit((StringLiteral) node, traceType);
        } else if (node instanceof SuperFieldAccess) {
            return visit((SuperFieldAccess) node, traceType);
        } else if (node instanceof SuperMethodInvocation) {
            return visit((SuperMethodInvocation) node, traceType);
        } else if (node instanceof SuperMethodReference) {
            return visit((SuperMethodReference) node, traceType);
        } else if (node instanceof ThisExpression) {
            return visit((ThisExpression) node, traceType);
        } else if (node instanceof TypeLiteral) {
            return visit((TypeLiteral) node, traceType);
        } else if (node instanceof TypeMethodReference) {
            return visit((TypeMethodReference) node, traceType);
        } else if (node instanceof VariableDeclarationExpression) {
            return visit((VariableDeclarationExpression) node, traceType);
        } else if (node instanceof AnonymousClassDeclaration) {
            return visit((AnonymousClassDeclaration) node, traceType);
        } else if (node instanceof VariableDeclarationFragment) {
            return visit((VariableDeclarationFragment) node, traceType);
        } else if (node instanceof SingleVariableDeclaration) {
            return visit((SingleVariableDeclaration) node, traceType);
        } else if (node instanceof MethodDeclaration) {
            return visit((MethodDeclaration) node, traceType);
        } else if (node instanceof CatchClause) {
            return visit((CatchClause) node, traceType);
        } else {
            LevelLogger.error("UNKNOWN ASTNode type : " + node.toString());
            return null;
        }
    }
}
