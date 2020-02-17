package trans.trace;

import org.eclipse.jdt.core.dom.*;
import trans.common.LevelLogger;

import java.util.ArrayList;

public class InstrumentationParser {
    private CompilationUnit _unit;
    private Statement tryCtrlStmt;

    /************************** Visit MethodDeclaration ***********************/

    private ASTNode visit(MethodDeclaration node, int type) {
        ArrayList<ASTNode> params = new ArrayList<>();
        for (Object arg : node.parameters())
            params.add(process((ASTNode) arg, TraceUtil.TRACE_TYPE_ENTRY));
        Block body = node.getBody();
        if (body != null) {
            Block newBody = (Block) process(body, type);
            node.setBody(TraceUtil.genBlock(params, newBody));
        }
        return node;
    }

    /****************************** Visit BLOCK *******************************/

    /**
     * Block:
     * { { Statement } }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(Block node, int type) {
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
    private ASTNode visit(ConstructorInvocation node, int type) {
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
    private ASTNode visit(SuperConstructorInvocation node, int type) {
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
    private ASTNode visit(AssertStatement node, int type) {
        AssertStatement myNode = (AssertStatement) TraceUtil.copyNode(node);
        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return myNode;
    }

    /**
     * BreakStatement:
     *	break [ Identifier ] ;
     */
    private ASTNode visit(BreakStatement node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ContinueStatement:
     * continue [ Identifier ] ;
     */
    private ASTNode visit(ContinueStatement node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * EmptyStatement:
     *	;
     */
    private ASTNode visit(EmptyStatement node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ExpressionStatement:
     * StatementExpression ;
     */
    private ASTNode visit(ExpressionStatement node, int type) {
        ExpressionStatement myNode = (ExpressionStatement) TraceUtil.copyNode(node);
        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return myNode;
    }

    /**
     * LabeledStatement:
     *	Identifier : Statement
     */
    private ASTNode visit(LabeledStatement node, int type) {
        LabeledStatement myNode = (LabeledStatement) TraceUtil.copyNode(node);
        if (node.getBody() != null)
            myNode.setBody((Statement) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE));
        return myNode;
    }

    /**
     * ReturnStatement:
     *	return [ Expression ] ;
     */
    private ASTNode visit(ReturnStatement node, int type) {
        ReturnStatement myNode = (ReturnStatement) TraceUtil.copyNode(node);
        if (node.getExpression() != null)
        {
            Expression expression = (Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE);
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            myNode.setExpression(TraceUtil.genReturnExpression(expression, line, column));
        }
        return myNode;
    }

    /**
     * SynchronizedStatement:
     *	synchronized ( Expression ) Block
     */
    private ASTNode visit(SynchronizedStatement node, int type) {
        SynchronizedStatement myNode = (SynchronizedStatement) TraceUtil.copyNode(node);

        if (node.getExpression() != null)
            myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement statement = TraceUtil.genControlStatement(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        myNode.setBody(TraceUtil.genBlock(statement, block));

        return myNode;
    }

    /**
     * TypeDeclarationStatement:
     *	TypeDeclaration
     *	EnumDeclaration
     */
    private ASTNode visit(TypeDeclarationStatement node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * VariableDeclarationStatement: { ExtendedModifier } Type
     * VariableDeclarationFragment { , VariableDeclarationFragment } ;
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(VariableDeclarationStatement node, int type) {
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
    private ASTNode visit(IfStatement node, int type) {
        IfStatement myNode = (IfStatement) TraceUtil.copyNode(node);

        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement thenStatementControl = TraceUtil.genControlStatement(line, column);
        Statement thenStatement = (Statement) process(node.getThenStatement(), type);
        if (thenStatement instanceof Block) {
            Block thenBlock = (Block) thenStatement;
            myNode.setThenStatement(TraceUtil.genBlock(thenStatementControl, thenBlock));
        }
        else myNode.setThenStatement(TraceUtil.genBlock(thenStatementControl, thenStatement));

        if (node.getElseStatement() != null) {
            Statement elseStatementControl = TraceUtil.genControlStatement(line, column);
            Statement elseStatement = (Statement) process(node.getElseStatement(), type);
            if (elseStatement instanceof Block) {
                Block elseBlock = (Block) elseStatement;
                myNode.setElseStatement(TraceUtil.genBlock(elseStatementControl, elseBlock));
            }
            else
                myNode.setElseStatement(TraceUtil.genBlock(elseStatementControl, elseStatement));
        }

        return myNode;
    }

    /**
     * SwitchCase:
     *           case Expression  :
     *           default :
     */
    private ASTNode visit(SwitchCase node, int type) {
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
    private ASTNode visit(SwitchStatement node, int type) {
        SwitchStatement myNode = (SwitchStatement) TraceUtil.copyNode(node);

        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        ArrayList<ASTNode> statements = new ArrayList<>();
        for (Object object : node.statements())
            if (!(object instanceof SwitchCase)) {
                Statement statementControl = TraceUtil.genControlStatement(line, column);
                Statement statement = (Statement) process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE);
                if (statement instanceof Block) {
                    Block block = (Block) statement;
                    statements.add(TraceUtil.genBlock(statementControl, block));
                }
                else
                    statements.add(TraceUtil.genBlock(statementControl, statement));
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
    private ASTNode visit(EnhancedForStatement node, int type) {
        EnhancedForStatement myNode = (EnhancedForStatement) TraceUtil.copyNode(node);

        myNode.setParameter((SingleVariableDeclaration) process(node.getParameter(), TraceUtil.TRACE_TYPE_NONE));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Expression expression = (Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE);
        myNode.setExpression(TraceUtil.genAssignExpression(expression, line, column));

        Statement statementControl = TraceUtil.genControlStatement(line, column);
        Statement statement = (Statement) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        if (statement instanceof Block) {
            Block block = (Block) statement;
            myNode.setBody(TraceUtil.genBlock(statementControl, block));
        }
        else myNode.setBody(TraceUtil.genBlock(statementControl, statement));

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
    private ASTNode visit(ForStatement node, int type) {
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
        Statement statementControl = TraceUtil.genControlStatement(line, column);
        Statement statement = (Statement) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        if (statement instanceof Block) {
            Block block = (Block) statement;
            myNode.setBody(TraceUtil.genBlock(statementControl, block));
        }
        else myNode.setBody(TraceUtil.genBlock(statementControl, statement));

        return myNode;
    }

    /**
     * DoStatement:
     *	do Statement while ( Expression ) ;
     */
    private ASTNode visit(DoStatement node, int type) {
        DoStatement myNode = (DoStatement) TraceUtil.copyNode(node);

        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement statementControl = TraceUtil.genControlStatement(line, column);
        Statement statement = (Statement) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        if (statement instanceof Block) {
            Block block = (Block) statement;
            myNode.setBody(TraceUtil.genBlock(statementControl, block));
        }
        else myNode.setBody(TraceUtil.genBlock(statementControl, statement));

        return myNode;
    }

    /**
     * WhileStatement:
     *	while ( Expression ) Statement
     */
    private ASTNode visit(WhileStatement node, int type) {
        WhileStatement myNode = (WhileStatement) TraceUtil.copyNode(node);

        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement statementControl = TraceUtil.genControlStatement(line, column);
        Statement statement = (Statement) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        if (statement instanceof Block) {
            Block block = (Block) statement;
            myNode.setBody(TraceUtil.genBlock(statementControl, block));
        }
        else myNode.setBody(TraceUtil.genBlock(statementControl, statement));

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
    private ASTNode visit(TryStatement node, int type) {
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
        tryCtrlStmt = TraceUtil.genControlStatement(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        myNode.setBody(TraceUtil.genBlock(tryCtrlStmt, block));

        ArrayList<ASTNode> catchClauses = new ArrayList<>();
        for (Object object : node.catchClauses()) {
            CatchClause catchClause = (CatchClause) object;
            catchClauses.add(process(catchClause, TraceUtil.TRACE_TYPE_NONE));
        }
        myNode.catchClauses().clear();
        myNode.catchClauses().addAll(catchClauses);

        if (node.getFinally() != null) {
            Block finallyBlock = (Block) process(node.getFinally(), TraceUtil.TRACE_TYPE_NONE);
            myNode.setFinally(TraceUtil.genBlock(tryCtrlStmt, finallyBlock));
        }

        return myNode;
    }

    /**
     * CatchClause
     *    catch ( SingleVariableDeclaration ) Block
     */
    private ASTNode visit(CatchClause node, int type) {
        CatchClause myNode = (CatchClause) TraceUtil.copyNode(node);

        Statement entryStatement = (Statement) process(node.getException(), TraceUtil.TRACE_TYPE_ENTRY);

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement statement = TraceUtil.genControlStatement(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);

        ArrayList<ASTNode> statements = new ArrayList<>();
        statements.add(tryCtrlStmt);
        statements.add(statement);
        statements.add(entryStatement);
        myNode.setBody(TraceUtil.genBlock(statements, block));

        return myNode;
    }

    /**
     * ThrowStatement:
     *	throw Expression ;
     */
    private ASTNode visit(ThrowStatement node, int type) {
        ThrowStatement myNode = (ThrowStatement) TraceUtil.copyNode(node);
        myNode.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return myNode;
    }

    /************************** Visit Expression ******************************/
    private Expression controlExpressionNode(Expression node, int type) {
        if (type == TraceUtil.TRACE_TYPE_CONTROL) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    private int checkType(int type) {
        if (type == TraceUtil.TRACE_TYPE_CONTROL)
            return TraceUtil.TRACE_TYPE_NONE;
        return type;
    }

    /**
     * Annotation:
     * NormalAnnotation
     * MarkerAnnotation
     * SingleMemberAnnotation
     */
    private ASTNode visit(Annotation node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ArrayAccess:
     * Expression [ Expression ]
     */
    private ASTNode visit(ArrayAccess node, int type) {
        ArrayAccess myNode = (ArrayAccess) TraceUtil.copyNode(node);
        myNode.setIndex((Expression) process(node.getIndex(), checkType(type)));
        return controlExpressionNode(myNode, type);
    }

    /**
     * ArrayCreation: new PrimitiveType [ Expression ] { [ Expression ] } { [ ]
     * } new TypeName [ < Type { , Type } > ] [ Expression ] { [ Expression ] }
     * { [ ] } new PrimitiveType [ ] { [ ] } ArrayInitializer new TypeName [ <
     * Type { , Type } > ] [ ] { [ ] } ArrayInitializer
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ArrayCreation node, int type) {
        ArrayCreation myNode = (ArrayCreation) TraceUtil.copyNode(node);

        ArrayList<ASTNode> dimensions = new ArrayList<>();
        for (Object object : node.dimensions())
            dimensions.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
        myNode.dimensions().clear();
        myNode.dimensions().addAll(dimensions);

        if (node.getInitializer() != null)
            myNode.setInitializer((ArrayInitializer) process(node.getInitializer(), type));

        return myNode;
    }

    /**
     * ArrayInitializer:
     *      { [ Expression { , Expression} [ , ]] }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ArrayInitializer node, int type) {
        ArrayInitializer myNode = (ArrayInitializer) TraceUtil.copyNode(node);

        ArrayList<ASTNode> expressions = new ArrayList<>();
        for (Object object : node.expressions())
            expressions.add(process((ASTNode) object, type));
        myNode.expressions().clear();
        myNode.expressions().addAll(expressions);

        return myNode;
    }

    /**
     * CastExpression:
     * ( Type ) Expression
     */
    private ASTNode visit(CastExpression node, int type) {
        CastExpression myNode = (CastExpression) TraceUtil.copyNode(node);
        node.setExpression((Expression) process(node.getExpression(), checkType(type)));
        return controlExpressionNode(myNode, type);
    }

    /**
     * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type
     * ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ClassInstanceCreation node, int type) {
        ClassInstanceCreation myNode = (ClassInstanceCreation) TraceUtil.copyNode(node);

        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, type));
        myNode.arguments().clear();
        myNode.arguments().addAll(arguments);

        return myNode;
    }

    private ASTNode visit(AnonymousClassDeclaration node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ConditionalExpression:
     *      Expression ? Expression : Expression
     */
    private ASTNode visit(ConditionalExpression node, int type) {
        ConditionalExpression myNode = (ConditionalExpression) TraceUtil.copyNode(node);
        int typeNow = checkType(type);

        myNode.setExpression((Expression) process(node.getExpression(), typeNow));
        myNode.setThenExpression((Expression) process(node.getThenExpression(), typeNow));
        myNode.setElseExpression((Expression) process(node.getElseExpression(), typeNow));

        return controlExpressionNode(myNode, type);
    }

    /**
     * InfixExpression:
     *		Expression InfixOperator Expression { InfixOperator Expression }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(InfixExpression node, int type) {
        InfixExpression myNode = (InfixExpression) TraceUtil.copyNode(node);

        int typeNow = type;
        if ((node.getOperator() != InfixExpression.Operator.CONDITIONAL_AND) &&
            (node.getOperator() != InfixExpression.Operator.CONDITIONAL_OR))
            typeNow = checkType(type);

        myNode.setLeftOperand((Expression) process(node.getLeftOperand(), typeNow));
        myNode.setRightOperand((Expression) process(node.getRightOperand(), typeNow));

        if (node.hasExtendedOperands()) {
            ArrayList<ASTNode> extendedOperands = new ArrayList<>();
            for (Object object : node.extendedOperands())
                extendedOperands.add(process((Expression) object, typeNow));
            myNode.extendedOperands().clear();
            myNode.extendedOperands().addAll(extendedOperands);
        }

        return controlExpressionNode(myNode, type);
    }

    /**
     * InstanceofExpression:
     *		Expression instanceof Type
     */
    private ASTNode visit(InstanceofExpression node, int type) {
        InstanceofExpression myNode = (InstanceofExpression) TraceUtil.copyNode(node);
        myNode.setLeftOperand((Expression) process(node.getLeftOperand(), checkType(type)));
        return controlExpressionNode(myNode, type);
    }

    /**
     * LambdaExpression:
     *	Identifier -> Body
     *	( [ Identifier { , Identifier } ] ) -> Body
     *	( [ FormalParameter { , FormalParameter } ] ) -> Body
     */
    private ASTNode visit(LambdaExpression node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     *  MethodInvocation:
     *  [ Expression . ]
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(MethodInvocation node, int type) {
        MethodInvocation myNode = (MethodInvocation) TraceUtil.copyNode(node);

        myNode.setExpression((Expression) process(node.getExpression(), checkType(type)));
        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, checkType(type)));
        myNode.arguments().clear();
        myNode.arguments().addAll(arguments);

        return controlExpressionNode(myNode, type);
    }

    /**
     * ParenthesizedExpression:
     *	( Expression )
     */
    private ASTNode visit(ParenthesizedExpression node, int type) {
        ParenthesizedExpression myNode = (ParenthesizedExpression) TraceUtil.copyNode(node);
        myNode.setExpression((Expression) process(node.getExpression(), type));
        return myNode;
    }

    /**
     * PostfixExpression:
     *	Expression PostfixOperator
     */
    private ASTNode visit(PostfixExpression node, int type) {
        PostfixExpression myNode = (PostfixExpression) TraceUtil.copyNode(node);
        myNode.setOperand((Expression) process(node.getOperand(), checkType(type)));
        return controlExpressionNode(myNode, type);
    }

    /**
     * PrefixExpression:
     *	PrefixOperator Expression
     */
    private ASTNode visit(PrefixExpression node, int type) {
        PrefixExpression myNode = (PrefixExpression) TraceUtil.copyNode(node);
        myNode.setOperand((Expression) process(node.getOperand(), checkType(type)));
        return controlExpressionNode(myNode, type);
    }

    /**
     * SuperMethodInvocation:
     *	[ ClassName . ] super .
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(SuperMethodInvocation node, int type) {
        SuperMethodInvocation myNode = (SuperMethodInvocation) TraceUtil.copyNode(node);

        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, checkType(type)));
        myNode.arguments().clear();
        myNode.arguments().addAll(arguments);

        return controlExpressionNode(myNode, type);
    }

    /**
     * Assignment:
     *      Expression AssignmentOperator Expression
     */
    private ASTNode visit(Assignment node, int type) {
        Assignment myNode = (Assignment) TraceUtil.copyNode(node);

        myNode.setLeftHandSide((Expression) process(node.getLeftHandSide(), checkType(type)));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Expression expression = (Expression) process(node.getRightHandSide(), checkType(type));
        myNode.setRightHandSide(TraceUtil.genAssignExpression(expression, line, column));

        return controlExpressionNode(myNode, type);
    }

    //-----------------------Declaration---------------------------
    /**
     * { ExtendedModifier } Type {Annotation} [ ... ] Identifier { Dimension } [ = Expression ]
     * "..." should not be appear since it is only used in method declarations
     */
    private ASTNode visit(SingleVariableDeclaration node, int type) {
        SingleVariableDeclaration myNode = (SingleVariableDeclaration) TraceUtil.copyNode(node);

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        if (type == TraceUtil.TRACE_TYPE_ENTRY)
            return TraceUtil.genEntryStatement(node.getName(), line, column);

        if (node.getInitializer() != null) {
            Expression expression = (Expression) process(node.getInitializer(), type);
            myNode.setInitializer(TraceUtil.genAssignExpression(expression, line, column));
        }

        return myNode;
    }

    /**
     * VariableDeclarationExpression:
     *	{ ExtendedModifier } Type VariableDeclarationFragment
     *	    { , VariableDeclarationFragment }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(VariableDeclarationExpression node, int type) {
        VariableDeclarationExpression myNode = (VariableDeclarationExpression) TraceUtil.copyNode(node);

        ArrayList<ASTNode> fragments = new ArrayList<>();
        for (Object object : node.fragments())
            fragments.add(process((ASTNode) object, type));
        myNode.fragments().clear();
        myNode.fragments().addAll(fragments);

        return myNode;
    }

    /**
     * VariableDeclarationFragment:
     *	Identifier { Dimension } [ = Expression ]
     */
    private ASTNode visit(VariableDeclarationFragment node, int type) {
        VariableDeclarationFragment myNode = (VariableDeclarationFragment) TraceUtil.copyNode(node);

        if (node.getInitializer() != null){
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            Expression expression = (Expression) process(node.getInitializer(), type);
            myNode.setInitializer(TraceUtil.genAssignExpression(expression, line, column));
        }

        return myNode;
    }

    //---------------------------Variable----------------------------
    /**
     * FieldAccess:
     *           Expression . Identifier
     */
    private ASTNode visit(FieldAccess node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * Name:
     *	SimpleName
     *	QualifiedName
     */
    private ASTNode visit(Name node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * SuperFieldAccess:
     *	[ ClassName . ] super . Identifier
     */
    private ASTNode visit(SuperFieldAccess node, int type) {
        return TraceUtil.copyNode(node);
    }

    //-------------------------Literal--------------------------
    /**
     * BooleanLiteral:
     *      true false
     */
    private ASTNode visit(BooleanLiteral node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * Character literal nodes.
     */
    private ASTNode visit(CharacterLiteral node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * Null literal node.
     */
    private ASTNode visit(NullLiteral node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * Number literal node.
     */
    private ASTNode visit(NumberLiteral node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * String literal nodes.
     */
    private ASTNode visit(StringLiteral node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * TypeLiteral:
     *	( Type | void ) . class
     */
    private ASTNode visit(TypeLiteral node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ThisExpression:
     *	[ ClassName . ] this
     */
    private ASTNode visit(ThisExpression node, int type) {
        return TraceUtil.copyNode(node);
    }

    //-------------------------Reference-------------------
    /**
     * CreationReference:
     *      Type ::
     *          [ < Type { , Type } > ]
     *      new
     */
    private ASTNode visit(CreationReference node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * ExpressionMethodReference:
     *	Expression ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private ASTNode visit(ExpressionMethodReference node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * MethodReference:
     *	CreationReference
     *	ExpressionMethodReference
     *	SuperMethodReference
     *	TypeMethodReference
     */
    private ASTNode visit(MethodReference node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * SuperMethodReference:
     *	[ ClassName . ] super ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private ASTNode visit(SuperMethodReference node, int type) {
        return TraceUtil.copyNode(node);
    }

    /**
     * TypeMethodReference:
     *	Type ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private ASTNode visit(TypeMethodReference node, int type) {
        return TraceUtil.copyNode(node);
    }


    /************************** Process ******************************/
    InstrumentationParser(CompilationUnit unit) {
        _unit = unit;
    }

    ASTNode process(ASTNode node) {
        return process(node, TraceUtil.TRACE_TYPE_NONE);
    }

    private ASTNode process(ASTNode node, int type) {
        if (node == null) {
            return null;
        }
        if (node instanceof AssertStatement) {
            return visit((AssertStatement) node, type);
        } else if (node instanceof Block) {
            return visit((Block) node, type);
        } else if (node instanceof BreakStatement) {
            return visit((BreakStatement) node, type);
        } else if (node instanceof ConstructorInvocation) {
            return visit((ConstructorInvocation) node, type);
        } else if (node instanceof ContinueStatement) {
            return visit((ContinueStatement) node, type);
        } else if (node instanceof DoStatement) {
            return visit((DoStatement) node, type);
        } else if (node instanceof EmptyStatement) {
            return visit((EmptyStatement) node, type);
        } else if (node instanceof EnhancedForStatement) {
            return visit((EnhancedForStatement) node, type);
        } else if (node instanceof ExpressionStatement) {
            return visit((ExpressionStatement) node, type);
        } else if (node instanceof ForStatement) {
            return visit((ForStatement) node, type);
        } else if (node instanceof IfStatement) {
            return visit((IfStatement) node, type);
        } else if (node instanceof LabeledStatement) {
            return visit((LabeledStatement) node, type);
        } else if (node instanceof ReturnStatement) {
            return visit((ReturnStatement) node, type);
        } else if (node instanceof SuperConstructorInvocation) {
            return visit((SuperConstructorInvocation) node, type);
        } else if (node instanceof SwitchCase) {
            return visit((SwitchCase) node, type);
        } else if (node instanceof SwitchStatement) {
            return visit((SwitchStatement) node, type);
        } else if (node instanceof SynchronizedStatement) {
            return visit((SynchronizedStatement) node, type);
        } else if (node instanceof ThrowStatement) {
            return visit((ThrowStatement) node, type);
        } else if (node instanceof TryStatement) {
            return visit((TryStatement) node, type);
        } else if (node instanceof TypeDeclarationStatement) {
            return visit((TypeDeclarationStatement) node, type);
        } else if (node instanceof VariableDeclarationStatement) {
            return visit((VariableDeclarationStatement) node, type);
        } else if (node instanceof WhileStatement) {
            return visit((WhileStatement) node, type);
        } else if (node instanceof Annotation) {
            return visit((Annotation) node, type);
        } else if (node instanceof ArrayAccess) {
            return visit((ArrayAccess) node, type);
        } else if (node instanceof ArrayCreation) {
            return visit((ArrayCreation) node, type);
        } else if (node instanceof ArrayInitializer) {
            return visit((ArrayInitializer) node, type);
        } else if (node instanceof Assignment) {
            return visit((Assignment) node, type);
        } else if (node instanceof BooleanLiteral) {
            return visit((BooleanLiteral) node, type);
        } else if (node instanceof CastExpression) {
            return visit((CastExpression) node, type);
        } else if (node instanceof CharacterLiteral) {
            return visit((CharacterLiteral) node, type);
        } else if (node instanceof ClassInstanceCreation) {
            return visit((ClassInstanceCreation) node, type);
        } else if (node instanceof ConditionalExpression) {
            return visit((ConditionalExpression) node, type);
        } else if (node instanceof CreationReference) {
            return visit((CreationReference) node, type);
        } else if (node instanceof ExpressionMethodReference) {
            return visit((ExpressionMethodReference) node, type);
        } else if (node instanceof FieldAccess) {
            return visit((FieldAccess) node, type);
        } else if (node instanceof InfixExpression) {
            return visit((InfixExpression) node, type);
        } else if (node instanceof InstanceofExpression) {
            return visit((InstanceofExpression) node, type);
        } else if (node instanceof LambdaExpression) {
            return visit((LambdaExpression) node, type);
        } else if (node instanceof MethodInvocation) {
            return visit((MethodInvocation) node, type);
        } else if (node instanceof MethodReference) {
            return visit((MethodReference) node, type);
        } else if (node instanceof Name) {
            return visit((Name) node, type);
        } else if (node instanceof NullLiteral) {
            return visit((NullLiteral) node, type);
        } else if (node instanceof NumberLiteral) {
            return visit((NumberLiteral) node, type);
        } else if (node instanceof ParenthesizedExpression) {
            return visit((ParenthesizedExpression) node, type);
        } else if (node instanceof PostfixExpression) {
            return visit((PostfixExpression) node, type);
        } else if (node instanceof PrefixExpression) {
            return visit((PrefixExpression) node, type);
        } else if (node instanceof StringLiteral) {
            return visit((StringLiteral) node, type);
        } else if (node instanceof SuperFieldAccess) {
            return visit((SuperFieldAccess) node, type);
        } else if (node instanceof SuperMethodInvocation) {
            return visit((SuperMethodInvocation) node, type);
        } else if (node instanceof SuperMethodReference) {
            return visit((SuperMethodReference) node, type);
        } else if (node instanceof ThisExpression) {
            return visit((ThisExpression) node, type);
        } else if (node instanceof TypeLiteral) {
            return visit((TypeLiteral) node, type);
        } else if (node instanceof TypeMethodReference) {
            return visit((TypeMethodReference) node, type);
        } else if (node instanceof VariableDeclarationExpression) {
            return visit((VariableDeclarationExpression) node, type);
        } else if (node instanceof AnonymousClassDeclaration) {
            return visit((AnonymousClassDeclaration) node, type);
        } else if (node instanceof VariableDeclarationFragment) {
            return visit((VariableDeclarationFragment) node, type);
        } else if (node instanceof SingleVariableDeclaration) {
            return visit((SingleVariableDeclaration) node, type);
        } else if (node instanceof MethodDeclaration) {
            return visit((MethodDeclaration) node, type);
        } else if (node instanceof CatchClause) {
            return visit((CatchClause) node, type);
        } else {
            LevelLogger.error("UNKNOWN ASTNode type : " + node.toString());
            return null;
        }
    }
}
