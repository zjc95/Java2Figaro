package trans.staticUtils;
import trans.common.LevelLogger;
import org.eclipse.jdt.core.dom.*;

class SourceParser {
    private static final int VARIABLE_TYPE_NONE = 0;
    private static final int VARIABLE_TYPE_DEF = 1;
    private static final int VARIABLE_TYPE_USE = 2;
    private StaticInfo _info;
    private CompilationUnit _unit;

    /************************** Visit MethodDeclaration ***********************/

    private Stmt visit(MethodDeclaration node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        //process(node.getName(), context.newStmtContext(stmt, VARIABLE_TYPE_NONE));
        Context contextParam = context.newStmtContext(stmt, VARIABLE_TYPE_DEF);
        for (Object arg : node.parameters())
            process((ASTNode) arg, contextParam);

        Block body = node.getBody();
        if (body != null)
            process(body, context);
        return null;
    }

    /****************************** Visit BLOCK *******************************/

    /**
     * Block:
     * { { Statement } }
     */
    private Stmt visit(Block node, Context context) {
        for (Object object : node.statements())
            process((ASTNode) object, context);
        return null;
    }

    /*************************** Visit Statement ******************************/

    /**
     * ConstructorInvocation:
     * [ < Type { , Type } > ]
     * this ( [ Expression { , Expression } ] ) ;
     */
    private Stmt visit(ConstructorInvocation node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        Context contextParam = context.newStmtContext(stmt, VARIABLE_TYPE_USE);
        for (Object object : node.arguments())
            process((ASTNode) object, contextParam);
        return null;
    }

    /**
     * SuperConstructorInvocation:
     *	[ Expression . ]
     *	    [ < Type { , Type } > ]
     *	    super ( [ Expression { , Expression } ] ) ;
     */
    private Stmt visit(SuperConstructorInvocation node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);
        //if (node.getExpression() != null)
        //    process(node.getExpression(), context.newStmtContext(stmt, VARIABLE_TYPE_NONE));
        Context contextParam = context.newStmtContext(stmt, VARIABLE_TYPE_USE);
        for (Object object : node.arguments())
            process((ASTNode) object, contextParam);
        return null;
    }

    /**
     * AssertStatement:
     *	assert Expression [ : Expression ] ;
     */
    private Stmt visit(AssertStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        //process(node.getExpression(), context.newStmtContext(stmt, VARIABLE_TYPE_NONE));
        process(node.getMessage(), context.newStmtContext(stmt, VARIABLE_TYPE_USE));
        return null;
    }

    /**
     * BreakStatement:
     *	break [ Identifier ] ;
     */
    private Stmt visit(BreakStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);
        return null;
    }

    /**
     * ContinueStatement:
     * continue [ Identifier ] ;
     */
    private Stmt visit(ContinueStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);
        return null;
    }

    /**
     * EmptyStatement:
     *	;
     */
    private Stmt visit(EmptyStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);
        return null;
    }

    /**
     * ExpressionStatement:
     * StatementExpression ;
     */
    private Stmt visit(ExpressionStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);
        process(node.getExpression(), context.newStmtContext(stmt, VARIABLE_TYPE_USE));
        return null;
    }

    /**
     * LabeledStatement:
     *	Identifier : Statement
     */
    private Stmt visit(LabeledStatement node, Context context) {
        if (node.getBody() != null)
            process(node.getBody(), context);
        return null;
    }

    /**
     * ReturnStatement:
     *	return [ Expression ] ;
     */
    private Stmt visit(ReturnStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);
        if (node.getExpression() != null)
            process(node.getExpression(), context.newStmtContext(stmt, VARIABLE_TYPE_USE));
        return null;
    }

    /**
     * SynchronizedStatement:
     *	synchronized ( Expression ) Block
     */
    private Stmt visit(SynchronizedStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);
        if (node.getExpression() != null)
            process(node.getExpression(), context.newStmtContext(stmt, VARIABLE_TYPE_USE));
        process(node.getBody(), context.newBranchContext(stmt));
        return null;
    }

    /**
     * TypeDeclarationStatement:
     *	TypeDeclaration
     *	EnumDeclaration
     */
    private Stmt visit(TypeDeclarationStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);
        return null;
    }

    /**
     * VariableDeclarationStatement: { ExtendedModifier } Type
     * VariableDeclarationFragment { , VariableDeclarationFragment } ;
     */
    private Stmt visit(VariableDeclarationStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        Context contextParam = context.newStmtContext(stmt, VARIABLE_TYPE_DEF);
        for (Object object : node.fragments())
            process((ASTNode) object, contextParam);
        return null;
    }

    //-------------------Branch------------------------
    /**
     * IfStatement:
     *	if ( Expression ) Statement [ else Statement]
     */
    private Stmt visit(IfStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        process(node.getExpression(), context.newConditionContext(stmt));
        Context contextBranch = context.newBranchContext(stmt);
        process(node.getThenStatement(), contextBranch);
        if (node.getElseStatement() != null)
            process(node.getElseStatement(), contextBranch);
        return null;
    }

    /**
     * SwitchCase:
     *           case Expression  :
     *           default :
     */
    private Stmt visit(SwitchCase node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        if (node.getExpression() != null)
            process(node.getExpression(), context.newStmtContext(stmt, VARIABLE_TYPE_USE));
        return stmt;
    }

    /**
     * SwitchStatement:
     *           switch ( Expression )
     *                   { { SwitchCase | Statement } }
     * SwitchCase:
     *           case Expression  :
     *           default :
     */
    private Stmt visit(SwitchStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);
        
        process(node.getExpression(), context.newStmtContext(stmt, VARIABLE_TYPE_USE));

        Context contextBranch = context.newBranchContext(stmt);
        for (Object object : node.statements())
            process((ASTNode) object, contextBranch);
        return null;
    }

    //--------------------Loop-------------------------
    /**
     * EnhancedForStatement:
     * for ( FormalParameter : Expression )
     * Statement
     */
    private Stmt visit(EnhancedForStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Assign assign = new Assign(context.getStmt(), line, column, node);
        Context contextExpr = context.newStmtContext(stmt, VARIABLE_TYPE_USE);

        process(node.getParameter(), contextExpr.newAssignContext(assign, VARIABLE_TYPE_DEF));
        process(node.getExpression(), contextExpr.newAssignContext(assign, VARIABLE_TYPE_USE));
        process(node.getBody(), context.newBranchContext(stmt));
        return null;
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
    private Stmt visit(ForStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        if (!node.initializers().isEmpty()) {
            Context contextInit = context.newStmtContext(stmt, VARIABLE_TYPE_USE);
            for (Object object : node.initializers())
                process((ASTNode) object, contextInit);
        }

        if (node.getExpression() != null)
            process(node.getExpression(), context.newConditionContext(stmt));

        if (!node.updaters().isEmpty()) {
            Context contextUpdate = context.newStmtContext(stmt, VARIABLE_TYPE_USE);
            for (Object object : node.updaters())
                process((ASTNode) object, contextUpdate);
        }

        process(node.getBody(), context.newBranchContext(stmt));
        return null;
    }

    /**
     * DoStatement:
     *	do Statement while ( Expression ) ;
     */
    private Stmt visit(DoStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        process(node.getExpression(), context.newConditionContext(stmt));
        process(node.getBody(), context.newBranchContext(stmt));
        return null;
    }

    /**
     * WhileStatement:
     *	while ( Expression ) Statement
     */
    private Stmt visit(WhileStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        process(node.getExpression(), context.newConditionContext(stmt));
        process(node.getBody(), context.newBranchContext(stmt));
        return null;
    }

    //--------------------Try-Catch-------------------
    /**
     * TryStatement:
     *	try [ ( Resources ) ]
     *	    Block
     *	    [ { CatchClause } ]
     *	    [ finally Block ]
     */
    private Stmt visit(TryStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        Context contextStmt = context.newStmtContext(stmt, VARIABLE_TYPE_USE);
        if (node.resources() != null)
            for (Object object : node.resources())
                process((ASTNode) object, contextStmt);

        Context contextBranch = context.newBranchContext(stmt);

        process(node.getBody(), contextBranch);

        for (Object object : node.catchClauses()) {
            CatchClause catchClause = (CatchClause) object;
            process(catchClause, contextBranch);
        }

        if (node.getFinally() != null)
            process(node.getFinally(), contextBranch);
        return null;
    }

    /**
     * CatchClause
     *    catch ( SingleVariableDeclaration ) Block
     */
    private Stmt visit(CatchClause node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        process(node.getException(), context.newStmtContext(stmt, VARIABLE_TYPE_DEF));
        process(node.getBody(), context.newBranchContext(stmt));
        return null;
    }

    /**
     * ThrowStatement:
     *	throw Expression ;
     */
    private Stmt visit(ThrowStatement node, Context context) {
        Stmt stmt = new Stmt(node, context.getStructure(), _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        _info.addStatement(stmt);

        process(node.getExpression(), context.newStmtContext(stmt, VARIABLE_TYPE_USE));
        return null;
    }

    /************************** Visit Expression ******************************/

    private Context checkCondition(ASTNode node, Context context) {
        if (!context.checkControl()) return context;
        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        ControlExpression control = new ControlExpression(line, column, node, context.getStmt());
        context.getStmt().addControlExpr(control);
        return context.newControlContext(control);
    }

    /**
     * Annotation:
     * NormalAnnotation
     * MarkerAnnotation
     * SingleMemberAnnotation
     */
    private Stmt visit(Annotation node, Context context) {
        return null;
    }

    /**
     * ArrayAccess:
     * Expression [ Expression ]
     */
    private Stmt visit(ArrayAccess node, Context context) {
        context = checkCondition(node, context);
        if (context.getType() == VARIABLE_TYPE_DEF) {
            process(node.getArray(), context);
            process(node.getIndex(), context.newExprContext());
        } else {
            process(node.getArray(), context);
            process(node.getIndex(), context);
        }
        return null;
    }

    /**
     * ArrayCreation: new PrimitiveType [ Expression ] { [ Expression ] } { [ ]
     * } new TypeName [ < Type { , Type } > ] [ Expression ] { [ Expression ] }
     * { [ ] } new PrimitiveType [ ] { [ ] } ArrayInitializer new TypeName [ <
     * Type { , Type } > ] [ ] { [ ] } ArrayInitializer
     */
    private Stmt visit(ArrayCreation node, Context context) {
        for (Object object : node.dimensions())
            process((ASTNode) object, context);
        if (node.getInitializer() != null)
            process(node.getInitializer(), context);
        return null;
    }

    /**
     * ArrayInitializer:
     *      { [ Expression { , Expression} [ , ]] }
     */
    private Stmt visit(ArrayInitializer node, Context context) {
        for (Object object : node.expressions())
            process((ASTNode) object, context);
        return null;
    }

    /**
     * CastExpression:
     * ( Type ) Expression
     */
    private Stmt visit(CastExpression node, Context context) {
        context = checkCondition(node, context);
        process(node.getExpression(), context);
        return null;
    }

    /**
     * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type
     * ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
     */
    private Stmt visit(ClassInstanceCreation node, Context context) {
        //if (node.getExpression() != null)
        //    process(node.getExpression(), context);
        //if (node.getAnonymousClassDeclaration() != null)
        //    process(node.getAnonymousClassDeclaration(), context);
        for (Object object : node.arguments())
            process((ASTNode) object, context.newExprContext());
        return null;
    }

    private Stmt visit(AnonymousClassDeclaration node, Context context) {
        return null;
    }

    /**
     * ConditionalExpression:
     *      Expression ? Expression : Expression
     */
    private Stmt visit(ConditionalExpression node, Context context) {
        context = checkCondition(node, context);
        process(node.getExpression(), context);
        process(node.getThenExpression(), context);
        process(node.getElseExpression(), context);
        return null;
    }

    /**
     * InfixExpression:
     *		Expression InfixOperator Expression { InfixOperator Expression }
     */
    private Stmt visit(InfixExpression node, Context context) {
        if ((node.getOperator() != InfixExpression.Operator.CONDITIONAL_AND) &&
            (node.getOperator() != InfixExpression.Operator.CONDITIONAL_OR))
            context = checkCondition(node, context);

        process(node.getLeftOperand(), context);
        process(node.getRightOperand(), context);
        if (node.hasExtendedOperands())
            for (Object object : node.extendedOperands())
                process((Expression) object, context);
        return null;
    }

    /**
     * InstanceofExpression:
     *		Expression instanceof Type
     */
    private Stmt visit(InstanceofExpression node, Context context) {
        context = checkCondition(node, context);
        process(node.getLeftOperand(), context);
        //process(node.getRightOperand(), context.newExprContext(VARIABLE_TYPE_NONE));
        return null;
    }

    /**
     * LambdaExpression:
     *	Identifier -> Body
     *	( [ Identifier { , Identifier } ] ) -> Body
     *	( [ FormalParameter { , FormalParameter } ] ) -> Body
     */
    private Stmt visit(LambdaExpression node, Context context) {
        return null;
    }

    /**
     *  MethodInvocation:
     *  [ Expression . ]
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
     */
    private Stmt visit(MethodInvocation node, Context context) {
        context = checkCondition(node, context);
        process(node.getExpression(), context);
        //process(node.getName(), context.newExprContext(VARIABLE_TYPE_NONE));
        for (Object object : node.arguments())
            process((ASTNode) object, context);
        return null;
    }

    /**
     * ParenthesizedExpression:
     *	( Expression )
     */
    private Stmt visit(ParenthesizedExpression node, Context context) {
        process(node.getExpression(), context);
        return null;
    }

    /**
     * PostfixExpression:
     *	Expression PostfixOperator
     */
    private Stmt visit(PostfixExpression node, Context context) {
        context = checkCondition(node, context);
        process(node.getOperand(), context);
        return null;
    }

    /**
     * PrefixExpression:
     *	PrefixOperator Expression
     */
    private Stmt visit(PrefixExpression node, Context context) {
        context = checkCondition(node, context);
        process(node.getOperand(), context);
        return null;
    }

    /**
     * SuperMethodInvocation:
     *	[ ClassName . ] super .
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
     */
    private Stmt visit(SuperMethodInvocation node, Context context) {
        context = checkCondition(node, context);
        //process(node.getName(), context.newExprContext(VARIABLE_TYPE_NONE));
        //if (node.getQualifier() != null)
        //    process(node.getQualifier(), context.newExprContext(VARIABLE_TYPE_NONE));
        for (Object object : node.arguments())
            process((ASTNode) object, context);
        return null;
    }

    /**
     * Assignment:
     *      Expression AssignmentOperator Expression
     */
    private Stmt visit(Assignment node, Context context) {
        context = checkCondition(node, context);
        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Assign assign = new Assign(context.getStmt(), line, column, node);

        process(node.getLeftHandSide(), context.newAssignContext(assign, VARIABLE_TYPE_DEF));
        process(node.getRightHandSide(), context.newAssignContext(assign, VARIABLE_TYPE_USE));

        if (!(node.getOperator().toString().equals("=")))
            assign.addUse(assign.getDef());

        if ((context.getAssign() != null) && (context.getType() == VARIABLE_TYPE_USE))
            context.getAssign().addUse(assign.getDef());
        return null;
    }

    //-----------------------Declaration---------------------------
    /**
     * { ExtendedModifier } Type {Annotation} [ ... ] Identifier { Dimension } [ = Expression ]
     * "..." should not be appear since it is only used in method declarations
     */
    private Stmt visit(SingleVariableDeclaration node, Context context) {
        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Assign assign = new Assign(context.getStmt(), line, column, node);
        process(node.getName(), context.newAssignContext(assign, VARIABLE_TYPE_DEF));
        if (node.getInitializer() != null)
            process(node.getInitializer(), context.newAssignContext(assign, VARIABLE_TYPE_USE));
        return null;
    }

    /**
     * VariableDeclarationExpression:
     *	{ ExtendedModifier } Type VariableDeclarationFragment
     *	    { , VariableDeclarationFragment }
     */
    private Stmt visit(VariableDeclarationExpression node, Context context) {
        for (Object object : node.fragments())
            process((ASTNode) object, context);
        return null;
    }

    /**
     * VariableDeclarationFragment:
     *	Identifier { Dimension } [ = Expression ]
     */
    private Stmt visit(VariableDeclarationFragment node, Context context) {
        int line = _unit.getLineNumber(node.getStartPosition());
        int column = _unit.getColumnNumber(node.getStartPosition());
        Assign assign = new Assign(context.getStmt(), line, column, node);
        process(node.getName(), context.newAssignContext(assign, VARIABLE_TYPE_DEF));
        if (node.getInitializer() != null)
            process(node.getInitializer(), context.newAssignContext(assign, VARIABLE_TYPE_USE));
        return null;
    }

    //---------------------------Variable----------------------------
    /**
     * FieldAccess:
     *           Expression . Identifier
     */
    private Stmt visit(FieldAccess node, Context context) {
        String varID = node.toString();
        VarNode var;
        switch (context.getType()){
            case VARIABLE_TYPE_DEF:
                var = _info.addVar(varID);
                context.getAssign().setDef(var);
                break;
            case VARIABLE_TYPE_USE:
                var = _info.addVar(varID);
                context.getStmt().addUse(var);
                if (context.getAssign() != null)
                    context.getAssign().addUse(var);
                if (context.getControlExpr() != null)
                    context.getControlExpr().addUse(var);
                break;
            default:
        }
        return null;
    }

    /**
     * Name:
     *	SimpleName
     *	QualifiedName
     */
    private Stmt visit(Name node, Context context) {
        String varID = node.toString();
        VarNode var;
        if (varID.equals("String") || varID.equals("Math"))
            return null;
        switch (context.getType()){
            case VARIABLE_TYPE_DEF:
                var = _info.addVar(varID);
                context.getAssign().setDef(var);
                break;
            case VARIABLE_TYPE_USE:
                var = _info.addVar(varID);
                context.getStmt().addUse(var);
                if (context.getAssign() != null)
                    context.getAssign().addUse(var);
                if (context.getControlExpr() != null)
                    context.getControlExpr().addUse(var);
                break;
            default:
        }
        return null;
    }

    /**
     * SuperFieldAccess:
     *	[ ClassName . ] super . Identifier
     */
    private Stmt visit(SuperFieldAccess node, Context context) {
        String varID = node.toString();
        VarNode var;
        switch (context.getType()){
            case VARIABLE_TYPE_DEF:
                var = _info.addVar(varID);
                context.getAssign().setDef(var);
                break;
            case VARIABLE_TYPE_USE:
                var = _info.addVar(varID);
                context.getStmt().addUse(var);
                if (context.getAssign() != null)
                    context.getAssign().addUse(var);
                if (context.getControlExpr() != null)
                    context.getControlExpr().addUse(var);
                break;
            default:
        }
        return null;
    }

    //-------------------------Literal--------------------------
    /**
     * BooleanLiteral:
     *      true false
     */
    private Stmt visit(BooleanLiteral node, Context context) {
        return null;
    }

    /**
     * Character literal nodes.
     */
    private Stmt visit(CharacterLiteral node, Context context) {
        return null;
    }

    /**
     * Null literal node.
     */
    private Stmt visit(NullLiteral node, Context context) {
        return null;
    }

    /**
     * Number literal node.
     */
    private Stmt visit(NumberLiteral node, Context context) {
        return null;
    }

    /**
     * String literal nodes.
     */
    private Stmt visit(StringLiteral node, Context context) {
        return null;
    }

    /**
     * TypeLiteral:
     *	( Type | void ) . class
     */
    private Stmt visit(TypeLiteral node, Context context) {
        return null;
    }

    /**
     * ThisExpression:
     *	[ ClassName . ] this
     */
    private Stmt visit(ThisExpression node, Context context) {
        return null;
    }

    //-------------------------Reference-------------------
    /**
     * CreationReference:
     *      Type ::
     *          [ < Type { , Type } > ]
     *      new
     */
    private Stmt visit(CreationReference node, Context context) {
        return null;
    }

    /**
     * ExpressionMethodReference:
     *	Expression ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private Stmt visit(ExpressionMethodReference node, Context context) {
        return null;
    }

    /**
     * MethodReference:
     *	CreationReference
     *	ExpressionMethodReference
     *	SuperMethodReference
     *	TypeMethodReference
     */
    private Stmt visit(MethodReference node, Context context) {
        return null;
    }

    /**
     * SuperMethodReference:
     *	[ ClassName . ] super ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private Stmt visit(SuperMethodReference node, Context context) {
        return null;
    }

    /**
     * TypeMethodReference:
     *	Type ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private Stmt visit(TypeMethodReference node, Context context) {
        return null;
    }


    /************************** Process ******************************/
    SourceParser(CompilationUnit unit, StaticInfo inform) {
        _unit = unit;
        _info = inform;
    }

    void process(ASTNode node) {
        process(node, new Context());
    }

    private Stmt process(ASTNode node, Context context) {
        if (node == null) {
            return null;
        }
        if (node instanceof AssertStatement) {
            return visit((AssertStatement) node, context);
        } else if (node instanceof Block) {
            return visit((Block) node, context);
        } else if (node instanceof BreakStatement) {
            return visit((BreakStatement) node, context);
        } else if (node instanceof ConstructorInvocation) {
            return visit((ConstructorInvocation) node, context);
        } else if (node instanceof ContinueStatement) {
            return visit((ContinueStatement) node, context);
        } else if (node instanceof DoStatement) {
            return visit((DoStatement) node, context);
        } else if (node instanceof EmptyStatement) {
            return visit((EmptyStatement) node, context);
        } else if (node instanceof EnhancedForStatement) {
            return visit((EnhancedForStatement) node, context);
        } else if (node instanceof ExpressionStatement) {
            return visit((ExpressionStatement) node, context);
        } else if (node instanceof ForStatement) {
            return visit((ForStatement) node, context);
        } else if (node instanceof IfStatement) {
            return visit((IfStatement) node, context);
        } else if (node instanceof LabeledStatement) {
            return visit((LabeledStatement) node, context);
        } else if (node instanceof ReturnStatement) {
            return visit((ReturnStatement) node, context);
        } else if (node instanceof SuperConstructorInvocation) {
            return visit((SuperConstructorInvocation) node, context);
        } else if (node instanceof SwitchCase) {
            return visit((SwitchCase) node, context);
        } else if (node instanceof SwitchStatement) {
            return visit((SwitchStatement) node, context);
        } else if (node instanceof SynchronizedStatement) {
            return visit((SynchronizedStatement) node, context);
        } else if (node instanceof ThrowStatement) {
            return visit((ThrowStatement) node, context);
        } else if (node instanceof TryStatement) {
            return visit((TryStatement) node, context);
        } else if (node instanceof TypeDeclarationStatement) {
            return visit((TypeDeclarationStatement) node, context);
        } else if (node instanceof VariableDeclarationStatement) {
            return visit((VariableDeclarationStatement) node, context);
        } else if (node instanceof WhileStatement) {
            return visit((WhileStatement) node, context);
        } else if (node instanceof Annotation) {
            return visit((Annotation) node, context);
        } else if (node instanceof ArrayAccess) {
            return visit((ArrayAccess) node, context);
        } else if (node instanceof ArrayCreation) {
            return visit((ArrayCreation) node, context);
        } else if (node instanceof ArrayInitializer) {
            return visit((ArrayInitializer) node, context);
        } else if (node instanceof Assignment) {
            return visit((Assignment) node, context);
        } else if (node instanceof BooleanLiteral) {
            return visit((BooleanLiteral) node, context);
        } else if (node instanceof CastExpression) {
            return visit((CastExpression) node, context);
        } else if (node instanceof CharacterLiteral) {
            return visit((CharacterLiteral) node, context);
        } else if (node instanceof ClassInstanceCreation) {
            return visit((ClassInstanceCreation) node, context);
        } else if (node instanceof ConditionalExpression) {
            return visit((ConditionalExpression) node, context);
        } else if (node instanceof CreationReference) {
            return visit((CreationReference) node, context);
        } else if (node instanceof ExpressionMethodReference) {
            return visit((ExpressionMethodReference) node, context);
        } else if (node instanceof FieldAccess) {
            return visit((FieldAccess) node, context);
        } else if (node instanceof InfixExpression) {
            return visit((InfixExpression) node, context);
        } else if (node instanceof InstanceofExpression) {
            return visit((InstanceofExpression) node, context);
        } else if (node instanceof LambdaExpression) {
            return visit((LambdaExpression) node, context);
        } else if (node instanceof MethodInvocation) {
            return visit((MethodInvocation) node, context);
        } else if (node instanceof MethodReference) {
            return visit((MethodReference) node, context);
        } else if (node instanceof Name) {
            return visit((Name) node, context);
        } else if (node instanceof NullLiteral) {
            return visit((NullLiteral) node, context);
        } else if (node instanceof NumberLiteral) {
            return visit((NumberLiteral) node, context);
        } else if (node instanceof ParenthesizedExpression) {
            return visit((ParenthesizedExpression) node, context);
        } else if (node instanceof PostfixExpression) {
            return visit((PostfixExpression) node, context);
        } else if (node instanceof PrefixExpression) {
            return visit((PrefixExpression) node, context);
        } else if (node instanceof StringLiteral) {
            return visit((StringLiteral) node, context);
        } else if (node instanceof SuperFieldAccess) {
            return visit((SuperFieldAccess) node, context);
        } else if (node instanceof SuperMethodInvocation) {
            return visit((SuperMethodInvocation) node, context);
        } else if (node instanceof SuperMethodReference) {
            return visit((SuperMethodReference) node, context);
        } else if (node instanceof ThisExpression) {
            return visit((ThisExpression) node, context);
        } else if (node instanceof TypeLiteral) {
            return visit((TypeLiteral) node, context);
        } else if (node instanceof TypeMethodReference) {
            return visit((TypeMethodReference) node, context);
        } else if (node instanceof VariableDeclarationExpression) {
            return visit((VariableDeclarationExpression) node, context);
        } else if (node instanceof AnonymousClassDeclaration) {
            return visit((AnonymousClassDeclaration) node, context);
        } else if (node instanceof VariableDeclarationFragment) {
            return visit((VariableDeclarationFragment) node, context);
        } else if (node instanceof SingleVariableDeclaration) {
            return visit((SingleVariableDeclaration) node, context);
        } else if (node instanceof MethodDeclaration) {
            return visit((MethodDeclaration) node, context);
        } else if (node instanceof CatchClause) {
            return visit((CatchClause) node, context);
        } else {
            LevelLogger.error("UNKNOWN ASTNode type : " + node.toString());
            return null;
        }
    }

    private static String typeToString(int type) {
        switch (type) {
            case VARIABLE_TYPE_NONE:
                return "VARIABLE_TYPE_NONE";
            case VARIABLE_TYPE_DEF:
                return "VARIABLE_TYPE_DEF";
            case VARIABLE_TYPE_USE:
                return "VARIABLE_TYPE_USE";
            default:
                LevelLogger.error("#typeToString Illegal type : " + type);
                return "ERROR";
        }
    }

    private class Context {
        private Stmt _structure = null;
        private Stmt _stmt = null;
        private Assign _assign = null;
        private ControlExpression _ctrlExpr = null;
        private int _type = VARIABLE_TYPE_NONE;
        private boolean _isCtrl = false;

        Context() {}

        Context(Context context) {
            _structure = context.getStructure();
            _stmt = context.getStmt();
            _assign = context.getAssign();
            _ctrlExpr = context.getControlExpr();
            _type = context.getType();
            _isCtrl = context.checkControl();
        }

        Context newStmtContext(Stmt stmt, int type) {
            Context contextNew = new Context();
            contextNew.setStructure(_structure);
            contextNew.setStmt(stmt);
            contextNew.setType(type);
            return contextNew;
        }
        
        Context newConditionContext(Stmt stmt) {
            Context contextNew = new Context();
            contextNew.setStructure(stmt);
            contextNew.setStmt(stmt);
            contextNew.setType(VARIABLE_TYPE_USE);
            contextNew.setControl(true);
            return contextNew;
        }

        Context newBranchContext(Stmt stmt) {
            Context contextNew = new Context();
            contextNew.setStructure(stmt);
            return contextNew;
        }

        Context newExprContext() {
            Context contextNew = new Context(this);
            contextNew.setType(VARIABLE_TYPE_USE);
            return contextNew;
        }

        Context newControlContext(ControlExpression expr) {
            Context contextNew = new Context(this);
            contextNew.setControlExpr(expr);
            contextNew.setControl(false);
            return contextNew;
        }

        Context newAssignContext(Assign assign, int type) {
            Context contextNew = new Context(this);
            contextNew.setAssign(assign);
            contextNew.setType(type);
            return contextNew;
        }

        Stmt getStmt() {
            return _stmt;
        }

        private void setStmt(Stmt stmt) {
            _stmt = stmt;
        }

        Assign getAssign() {
            return _assign;
        }

        private void setAssign(Assign assign) {
            _assign = assign;
        }

        Stmt getStructure() {
            return _structure;
        }

        private void setStructure(Stmt stmt) {
            _structure = stmt;
        }

        ControlExpression getControlExpr() {
            return _ctrlExpr;
        }

        private void setControlExpr(ControlExpression control) {
            _ctrlExpr = control;
        }

        int getType() {
            return _type;
        }

        private void setType(int type) {
            _type = type;
        }

        boolean checkControl() {
            return _isCtrl;
        }

        private void setControl(boolean isCond) {
            _isCtrl = isCond;
        }

    }
}




