package trans.trace;

import org.eclipse.jdt.core.dom.*;
import trans.common.LevelLogger;

import java.util.ArrayList;

public class InstrumentationParser {
    private static AST ast = AST.newAST(AST.JLS8);
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
        Block block = ast.newBlock();
        for (Object object : node.statements()) {
            ASTNode astNode = process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE);
            if (astNode instanceof Block) {
                Block innerBlock = (Block) astNode;
                for (Object stmt : innerBlock.statements())
                    block.statements().add(stmt);
            }
            else
                block.statements().add(astNode);
        }
        return null;
    }

    /*************************** Visit Statement ******************************/

    /**
     * ConstructorInvocation:
     * [ < Type { , Type } > ]
     * this ( [ Expression { , Expression } ] ) ;
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ConstructorInvocation node, int type) {
        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
        node.arguments().clear();
        node.arguments().addAll(arguments);
        return node;
    }

    /**
     * SuperConstructorInvocation:
     *	[ Expression . ]
     *	    [ < Type { , Type } > ]
     *	    super ( [ Expression { , Expression } ] ) ;
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(SuperConstructorInvocation node, int type) {
        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
        node.arguments().clear();
        node.arguments().addAll(arguments);
        return node;
    }

    /**
     * AssertStatement:
     *	assert Expression [ : Expression ] ;
     */
    private ASTNode visit(AssertStatement node, int type) {
        node.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return node;
    }

    /**
     * BreakStatement:
     *	break [ Identifier ] ;
     */
    private ASTNode visit(BreakStatement node, int type) {
        return node;
    }

    /**
     * ContinueStatement:
     * continue [ Identifier ] ;
     */
    private ASTNode visit(ContinueStatement node, int type) {
        return node;
    }

    /**
     * EmptyStatement:
     *	;
     */
    private ASTNode visit(EmptyStatement node, int type) {
        return node;
    }

    /**
     * ExpressionStatement:
     * StatementExpression ;
     */
    private ASTNode visit(ExpressionStatement node, int type) {
        node.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return node;
    }

    /**
     * LabeledStatement:
     *	Identifier : Statement
     */
    private ASTNode visit(LabeledStatement node, int type) {
        if (node.getBody() != null)
            node.setBody((Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE));
        return node;
    }

    /**
     * ReturnStatement:
     *	return [ Expression ] ;
     */
    private ASTNode visit(ReturnStatement node, int type) {
        if (node.getExpression() != null)
        {
            Expression expression = (Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE);
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            node.setExpression(TraceUtil.genReturnExpression(expression, line, column));
        }
        return node;
    }

    /**
     * SynchronizedStatement:
     *	synchronized ( Expression ) Block
     */
    private ASTNode visit(SynchronizedStatement node, int type) {
        if (node.getExpression() != null)
            node.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement statement = TraceUtil.genControlStatement(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        node.setBody(TraceUtil.genBlock(statement, block));
        return node;
    }

    /**
     * TypeDeclarationStatement:
     *	TypeDeclaration
     *	EnumDeclaration
     */
    private ASTNode visit(TypeDeclarationStatement node, int type) {
        return node;
    }

    /**
     * VariableDeclarationStatement: { ExtendedModifier } Type
     * VariableDeclarationFragment { , VariableDeclarationFragment } ;
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(VariableDeclarationStatement node, int type) {
        ArrayList<ASTNode> fragments = new ArrayList<>();
        for (Object object : node.fragments())
            fragments.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
        node.fragments().clear();
        node.fragments().addAll(fragments);
        return node;
    }

    //-------------------Branch------------------------
    /**
     * IfStatement:
     *	if ( Expression ) Statement [ else Statement]
     */
    private ASTNode visit(IfStatement node, int type) {
        node.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement thenStatement = TraceUtil.genControlStatement(line, column);
        Block thenBlock = (Block) process(node.getThenStatement(), type);
        node.setThenStatement(TraceUtil.genBlock(thenStatement, thenBlock));

        if (node.getElseStatement() != null) {
            Statement elseStatement = TraceUtil.genControlStatement(line, column);
            Block elseBlock = (Block) process(node.getThenStatement(), type);
            node.setElseStatement(TraceUtil.genBlock(elseStatement, elseBlock));
        }
        return node;
    }

    /**
     * SwitchCase:
     *           case Expression  :
     *           default :
     */
    private ASTNode visit(SwitchCase node, int type) {
        if (node.getExpression() != null)
            node.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return node;
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
        node.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        ArrayList<ASTNode> statements = new ArrayList<>();

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        for (Object object : node.statements())
            if (object instanceof Block) {
                Statement statement = TraceUtil.genControlStatement(line, column);
                Block block = (Block) process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE);
                statements.add(TraceUtil.genBlock(statement, block));
            }
        node.statements().clear();
        node.statements().addAll(statements);
        return node;
    }

    //--------------------Loop-------------------------
    /**
     * EnhancedForStatement:
     * for ( FormalParameter : Expression )
     * Statement
     */
    private ASTNode visit(EnhancedForStatement node, int type) {

        node.setParameter((SingleVariableDeclaration) process(node.getParameter(), TraceUtil.TRACE_TYPE_NONE));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Expression expression = (Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE);
        node.setExpression(TraceUtil.genAssignExpression(expression, line, column));

        Statement statement = TraceUtil.genControlStatement(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        node.setBody(TraceUtil.genBlock(statement, block));
        return node;
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
        if (!node.initializers().isEmpty()) {
            ArrayList<ASTNode> initializers = new ArrayList<>();
            for (Object object : node.initializers())
                initializers.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
            node.initializers().clear();
            node.initializers().addAll(initializers);
        }

        if (node.getExpression() != null)
            node.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        if (!node.updaters().isEmpty()) {
            ArrayList<ASTNode> updaters = new ArrayList<>();
            for (Object object : node.updaters())
                updaters.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
            node.initializers().clear();
            node.initializers().addAll(updaters);
        }

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement statement = TraceUtil.genControlStatement(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        node.setBody(TraceUtil.genBlock(statement, block));
        return node;
    }

    /**
     * DoStatement:
     *	do Statement while ( Expression ) ;
     */
    private ASTNode visit(DoStatement node, int type) {
        node.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement statement = TraceUtil.genControlStatement(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        node.setBody(TraceUtil.genBlock(statement, block));
        return node;
    }

    /**
     * WhileStatement:
     *	while ( Expression ) Statement
     */
    private ASTNode visit(WhileStatement node, int type) {
        node.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_CONTROL));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement statement = TraceUtil.genControlStatement(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        node.setBody(TraceUtil.genBlock(statement, block));
        return node;
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
        if (node.resources() != null) {
            ArrayList<ASTNode> resources = new ArrayList<>();
            for (Object object : node.resources())
                resources.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
            node.resources().clear();
            node.resources().addAll(resources);
        }

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        tryCtrlStmt = TraceUtil.genControlStatement(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        node.setBody(TraceUtil.genBlock(tryCtrlStmt, block));

        ArrayList<ASTNode> catchClauses = new ArrayList<>();
        for (Object object : node.catchClauses()) {
            CatchClause catchClause = (CatchClause) object;
            catchClauses.add(process(catchClause, TraceUtil.TRACE_TYPE_NONE));
        }
        node.catchClauses().clear();
        node.catchClauses().addAll(catchClauses);

        if (node.getFinally() != null) {
            Block finallyBlock = (Block) process(node.getFinally(), TraceUtil.TRACE_TYPE_NONE);
            node.setFinally(TraceUtil.genBlock(tryCtrlStmt, finallyBlock));
        }
        return node;
    }

    /**
     * CatchClause
     *    catch ( SingleVariableDeclaration ) Block
     */
    private ASTNode visit(CatchClause node, int type) {
        Statement entryStatement = (Statement) process(node.getException(), TraceUtil.TRACE_TYPE_ENTRY);

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Statement statement = TraceUtil.genControlStatement(line, column);
        Block block = (Block) process(node.getBody(), TraceUtil.TRACE_TYPE_NONE);
        node.setBody(TraceUtil.genBlock(tryCtrlStmt, TraceUtil.genBlock(statement, TraceUtil.genBlock(entryStatement, block))));
        return node;
    }

    /**
     * ThrowStatement:
     *	throw Expression ;
     */
    private ASTNode visit(ThrowStatement node, int type) {
        node.setExpression((Expression) process(node.getExpression(), TraceUtil.TRACE_TYPE_NONE));
        return node;
    }

    /************************** Visit Expression ******************************/
    /**
     * Annotation:
     * NormalAnnotation
     * MarkerAnnotation
     * SingleMemberAnnotation
     */
    private ASTNode visit(Annotation node, int type) {
        return node;
    }

    /**
     * ArrayAccess:
     * Expression [ Expression ]
     */
    private ASTNode visit(ArrayAccess node, int type) {
        int typeNow = type == TraceUtil.TRACE_TYPE_CONTROL ? TraceUtil.TRACE_TYPE_NONE : type;

        node.setIndex((Expression) process(node.getIndex(), typeNow));

        if (type == TraceUtil.TRACE_TYPE_CONTROL) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    /**
     * ArrayCreation: new PrimitiveType [ Expression ] { [ Expression ] } { [ ]
     * } new TypeName [ < Type { , Type } > ] [ Expression ] { [ Expression ] }
     * { [ ] } new PrimitiveType [ ] { [ ] } ArrayInitializer new TypeName [ <
     * Type { , Type } > ] [ ] { [ ] } ArrayInitializer
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ArrayCreation node, int type) {
        ArrayList<ASTNode> dimensions = new ArrayList<>();
        for (Object object : node.dimensions())
            dimensions.add(process((ASTNode) object, TraceUtil.TRACE_TYPE_NONE));
        node.dimensions().clear();
        node.dimensions().addAll(dimensions);

        if (node.getInitializer() != null)
            node.setInitializer((ArrayInitializer) process(node.getInitializer(), type));
        return null;
    }

    /**
     * ArrayInitializer:
     *      { [ Expression { , Expression} [ , ]] }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ArrayInitializer node, int type) {
        ArrayList<ASTNode> expressions = new ArrayList<>();
        for (Object object : node.expressions())
            expressions.add(process((ASTNode) object, type));
        node.expressions().clear();
        node.expressions().addAll(expressions);
        return null;
    }

    /**
     * CastExpression:
     * ( Type ) Expression
     */
    private ASTNode visit(CastExpression node, int type) {
        int typeNow = type == TraceUtil.TRACE_TYPE_CONTROL ? TraceUtil.TRACE_TYPE_NONE : type;

        node.setExpression((Expression) process(node.getExpression(), typeNow));

        if (type == TraceUtil.TRACE_TYPE_CONTROL) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    /**
     * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type
     * ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(ClassInstanceCreation node, int type) {
        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, type));
        node.arguments().clear();
        node.arguments().addAll(arguments);
        return node;
    }

    private ASTNode visit(AnonymousClassDeclaration node, int type) {
        return node;
    }

    /**
     * ConditionalExpression:
     *      Expression ? Expression : Expression
     */
    private ASTNode visit(ConditionalExpression node, int type) {
        int typeNow = type == TraceUtil.TRACE_TYPE_CONTROL ? TraceUtil.TRACE_TYPE_NONE : type;

        node.setExpression((Expression) process(node.getExpression(), typeNow));
        node.setThenExpression((Expression) process(node.getThenExpression(), typeNow));
        node.setElseExpression((Expression) process(node.getElseExpression(), typeNow));

        if (type == TraceUtil.TRACE_TYPE_CONTROL) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    /**
     * InfixExpression:
     *		Expression InfixOperator Expression { InfixOperator Expression }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(InfixExpression node, int type) {
        int typeNow = type;
        if ((node.getOperator() != InfixExpression.Operator.CONDITIONAL_AND) &&
            (node.getOperator() != InfixExpression.Operator.CONDITIONAL_OR))
            typeNow = type == TraceUtil.TRACE_TYPE_CONTROL ? TraceUtil.TRACE_TYPE_NONE : type;

        node.setLeftOperand((Expression) process(node.getLeftOperand(), typeNow));
        node.setRightOperand((Expression) process(node.getRightOperand(), typeNow));

        if (node.hasExtendedOperands()) {
            ArrayList<ASTNode> extendedOperands = new ArrayList<>();
            for (Object object : node.extendedOperands())
                extendedOperands.add(process((Expression) object, typeNow));
            node.extendedOperands().clear();
            node.extendedOperands().addAll(extendedOperands);
        }

        if (type != typeNow) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    /**
     * InstanceofExpression:
     *		Expression instanceof Type
     */
    private ASTNode visit(InstanceofExpression node, int type) {
        int typeNow = type == TraceUtil.TRACE_TYPE_CONTROL ? TraceUtil.TRACE_TYPE_NONE : type;

        node.setLeftOperand((Expression) process(node.getLeftOperand(), typeNow));

        if (type == TraceUtil.TRACE_TYPE_CONTROL) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    /**
     * LambdaExpression:
     *	Identifier -> Body
     *	( [ Identifier { , Identifier } ] ) -> Body
     *	( [ FormalParameter { , FormalParameter } ] ) -> Body
     */
    private ASTNode visit(LambdaExpression node, int type) {
        return node;
    }

    /**
     *  MethodInvocation:
     *  [ Expression . ]
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(MethodInvocation node, int type) {
        int typeNow = type == TraceUtil.TRACE_TYPE_CONTROL ? TraceUtil.TRACE_TYPE_NONE : type;

        node.setExpression((Expression) process(node.getExpression(), typeNow));
        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, typeNow));
        node.arguments().clear();
        node.arguments().addAll(arguments);

        if (type == TraceUtil.TRACE_TYPE_CONTROL) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    /**
     * ParenthesizedExpression:
     *	( Expression )
     */
    private ASTNode visit(ParenthesizedExpression node, int type) {
        node.setExpression((Expression) process(node.getExpression(), type));
        return node;
    }

    /**
     * PostfixExpression:
     *	Expression PostfixOperator
     */
    private ASTNode visit(PostfixExpression node, int type) {
        int typeNow = type == TraceUtil.TRACE_TYPE_CONTROL ? TraceUtil.TRACE_TYPE_NONE : type;

        node.setOperand((Expression) process(node.getOperand(), typeNow));

        if (type == TraceUtil.TRACE_TYPE_CONTROL) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    /**
     * PrefixExpression:
     *	PrefixOperator Expression
     */
    private ASTNode visit(PrefixExpression node, int type) {
        int typeNow = type == TraceUtil.TRACE_TYPE_CONTROL ? TraceUtil.TRACE_TYPE_NONE : type;

        node.setOperand((Expression) process(node.getOperand(), typeNow));

        if (type == TraceUtil.TRACE_TYPE_CONTROL) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    /**
     * SuperMethodInvocation:
     *	[ ClassName . ] super .
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(SuperMethodInvocation node, int type) {
        int typeNow = type == TraceUtil.TRACE_TYPE_CONTROL ? TraceUtil.TRACE_TYPE_NONE : type;

        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (Object object : node.arguments())
            arguments.add(process((ASTNode) object, typeNow));
        node.arguments().clear();
        node.arguments().addAll(arguments);

        if (type == TraceUtil.TRACE_TYPE_CONTROL) {
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    /**
     * Assignment:
     *      Expression AssignmentOperator Expression
     */
    private ASTNode visit(Assignment node, int type) {
        int typeNow = type == TraceUtil.TRACE_TYPE_CONTROL ? TraceUtil.TRACE_TYPE_NONE : type;

        node.setLeftHandSide((Expression) process(node.getLeftHandSide(), typeNow));

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Expression expression = (Expression) process(node.getRightHandSide(), typeNow);
        node.setRightHandSide(TraceUtil.genAssignExpression(expression, line, column));

        if (type == TraceUtil.TRACE_TYPE_CONTROL) {
            return TraceUtil.genControlExpression(node, line, column);
        }
        return node;
    }

    //-----------------------Declaration---------------------------
    /**
     * { ExtendedModifier } Type {Annotation} [ ... ] Identifier { Dimension } [ = Expression ]
     * "..." should not be appear since it is only used in method declarations
     */
    private ASTNode visit(SingleVariableDeclaration node, int type) {
        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        if (type == TraceUtil.TRACE_TYPE_ENTRY)
            return TraceUtil.genEntryStatement(node.getName(), line, column);

        if (node.getInitializer() != null) {
            Expression expression = (Expression) process( node.getInitializer(), type);
            node.setInitializer(TraceUtil.genAssignExpression(expression, line, column));
        }
        return node;
    }

    /**
     * VariableDeclarationExpression:
     *	{ ExtendedModifier } Type VariableDeclarationFragment
     *	    { , VariableDeclarationFragment }
     */
    @SuppressWarnings("unchecked")
    private ASTNode visit(VariableDeclarationExpression node, int type) {
        ArrayList<ASTNode> fragments = new ArrayList<>();
        for (Object object : node.fragments())
            fragments.add(process((ASTNode) object, type));
        node.fragments().clear();
        node.fragments().addAll(fragments);
        return node;
    }

    /**
     * VariableDeclarationFragment:
     *	Identifier { Dimension } [ = Expression ]
     */
    private ASTNode visit(VariableDeclarationFragment node, int type) {
        if (node.getInitializer() != null){
            int line = _unit.getLineNumber(node.getStartPosition());
            int column = _unit.getColumnNumber(node.getStartPosition());
            Expression expression = (Expression) process(node.getInitializer(), type);
            node.setInitializer(TraceUtil.genAssignExpression(expression, line, column));
        }
        return node;
    }

    //---------------------------Variable----------------------------
    /**
     * FieldAccess:
     *           Expression . Identifier
     */
    private ASTNode visit(FieldAccess node, int type) {
        return node;
    }

    /**
     * Name:
     *	SimpleName
     *	QualifiedName
     */
    private ASTNode visit(Name node, int type) {
        return node;
    }

    /**
     * SuperFieldAccess:
     *	[ ClassName . ] super . Identifier
     */
    private ASTNode visit(SuperFieldAccess node, int type) {
        return node;
    }

    //-------------------------Literal--------------------------
    /**
     * BooleanLiteral:
     *      true false
     */
    private ASTNode visit(BooleanLiteral node, int type) {
        return node;
    }

    /**
     * Character literal nodes.
     */
    private ASTNode visit(CharacterLiteral node, int type) {
        return node;
    }

    /**
     * Null literal node.
     */
    private ASTNode visit(NullLiteral node, int type) {
        return node;
    }

    /**
     * Number literal node.
     */
    private ASTNode visit(NumberLiteral node, int type) {
        return node;
    }

    /**
     * String literal nodes.
     */
    private ASTNode visit(StringLiteral node, int type) {
        return node;
    }

    /**
     * TypeLiteral:
     *	( Type | void ) . class
     */
    private ASTNode visit(TypeLiteral node, int type) {
        return node;
    }

    /**
     * ThisExpression:
     *	[ ClassName . ] this
     */
    private ASTNode visit(ThisExpression node, int type) {
        return node;
    }

    //-------------------------Reference-------------------
    /**
     * CreationReference:
     *      Type ::
     *          [ < Type { , Type } > ]
     *      new
     */
    private ASTNode visit(CreationReference node, int type) {
        return node;
    }

    /**
     * ExpressionMethodReference:
     *	Expression ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private ASTNode visit(ExpressionMethodReference node, int type) {
        return node;
    }

    /**
     * MethodReference:
     *	CreationReference
     *	ExpressionMethodReference
     *	SuperMethodReference
     *	TypeMethodReference
     */
    private ASTNode visit(MethodReference node, int type) {
        return node;
    }

    /**
     * SuperMethodReference:
     *	[ ClassName . ] super ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private ASTNode visit(SuperMethodReference node, int type) {
        return node;
    }

    /**
     * TypeMethodReference:
     *	Type ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private ASTNode visit(TypeMethodReference node, int type) {
        return node;
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
