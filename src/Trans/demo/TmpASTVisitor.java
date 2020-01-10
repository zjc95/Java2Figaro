package Trans.demo;

import jdk.nashorn.internal.codegen.CompileUnit;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;

public class TmpASTVisitor {

    private static final int VARIABLE_TYPE_NONE = 0;
    private static final int VARIABLE_TYPE_DEF = 1;
    private static final int VARIABLE_TYPE_USE = 2;
    private SourceInform _inform;
    private CompilationUnit _unit;
    private String _file;

    /************************** Visit MethodDeclaration ***********************/

    private VarNode visit(MethodDeclaration node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        process(node.getName(), null, null, VARIABLE_TYPE_NONE);
        for (Object arg : node.parameters()) {
            process((ASTNode) arg, stmt, assign, VARIABLE_TYPE_USE);
        }
        Block body = node.getBody();
        if (body != null) {
            process(body, null, null, VARIABLE_TYPE_NONE);
        }
        return null;
    }

    /****************************** Visit BLOCK *******************************/

    /**
     * Block:
     * { { Statement } }
     */
    private VarNode visit(Block node, Stmt stmt, Assign assign, int type) {
        for (Object object : node.statements())
            process((ASTNode) object, null, null, VARIABLE_TYPE_NONE);
        return null;
    }

    /*************************** Visit Statement ******************************/

    /**
     * ConstructorInvocation:
     * [ < Type { , Type } > ]
     * this ( [ Expression { , Expression } ] ) ;
     */
    private VarNode visit(ConstructorInvocation node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        for (Object object : node.arguments())
            process((ASTNode) object, stmt, null, VARIABLE_TYPE_USE);
        return null;
    }

    /**
     * SuperConstructorInvocation:
     *	[ Expression . ]
     *	    [ < Type { , Type } > ]
     *	    super ( [ Expression { , Expression } ] ) ;
     */
    private VarNode visit(SuperConstructorInvocation node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        if (node.getExpression() != null)
            process(node.getExpression(), stmt, null, VARIABLE_TYPE_NONE);
        for (Object object : node.arguments())
            process((ASTNode) object, stmt, null, VARIABLE_TYPE_USE);
        return null;
    }

    /**
     * AssertStatement:
     *	assert Expression [ : Expression ] ;
     */
    private VarNode visit(AssertStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        process(node.getExpression(), stmt, null, VARIABLE_TYPE_NONE);
        process(node.getMessage(), stmt, null, VARIABLE_TYPE_NONE);
        return null;
    }

    /**
     * BreakStatement:
     *	break [ Identifier ] ;
     */
    private VarNode visit(BreakStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        return null;
    }

    /**
     * ContinueStatement:
     * continue [ Identifier ] ;
     */
    private VarNode visit(ContinueStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        return null;
    }

    /**
     * EmptyStatement:
     *	;
     */
    private VarNode visit(EmptyStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        return null;
    }

    /**
     * ExpressionStatement:
     * StatementExpression ;
     */
    private VarNode visit(ExpressionStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        return null;
    }

    /**
     * LabeledStatement:
     *	Identifier : Statement
     */
    private VarNode visit(LabeledStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        if (node.getBody() != null)
            process(node.getBody(), null, null, VARIABLE_TYPE_NONE);
        return null;
    }

    /**
     * ReturnStatement:
     *	return [ Expression ] ;
     */
    private VarNode visit(ReturnStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        if (node.getExpression() != null)
            process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        return null;
    }

    /**
     * SynchronizedStatement:
     *	synchronized ( Expression ) Block
     */
    private VarNode visit(SynchronizedStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        if (node.getExpression() != null)
            process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        process(node.getBody(), null, null, VARIABLE_TYPE_NONE);
        return null;
    }

    /**
     * TypeDeclarationStatement:
     *	TypeDeclaration
     *	EnumDeclaration
     */
    private VarNode visit(TypeDeclarationStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        return null;
    }

    /**
     * VariableDeclarationStatement: { ExtendedModifier } Type
     * VariableDeclarationFragment { , VariableDeclarationFragment } ;
     */
    private VarNode visit(VariableDeclarationStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        for (Object object : node.fragments())
            process((ASTNode) object, stmt, null, VARIABLE_TYPE_DEF);
        return null;
    }

    //-------------------Branch------------------------
    /**
     * IfStatement:
     *	if ( Expression ) Statement [ else Statement]
     */
    private VarNode visit(IfStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        process(node.getThenStatement(), null, null, VARIABLE_TYPE_NONE);
        if (node.getElseStatement() != null)
            process(node.getElseStatement(), null, null, VARIABLE_TYPE_NONE);
        return null;
    }

    /**
     * SwitchCase:
     *           case Expression  :
     *           default :
     */
    private VarNode visit(SwitchCase node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        if (node.getExpression() != null)
            process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        return null;
    }

    /**
     * SwitchStatement:
     *           switch ( Expression )
     *                   { { SwitchCase | Statement } }
     * SwitchCase:
     *           case Expression  :
     *           default :
     */
    private VarNode visit(SwitchStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        for (Object object : node.statements())
            process((ASTNode) object, null, null, VARIABLE_TYPE_NONE);
        return null;
    }

    //--------------------Loop-------------------------
    /**
     * EnhancedForStatement:
     * for ( FormalParameter : Expression )
     * Statement
     */
    private VarNode visit(EnhancedForStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        process(node.getParameter(), stmt, null, VARIABLE_TYPE_USE);
        process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        process(node.getBody(), null, null, VARIABLE_TYPE_NONE);
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
    private VarNode visit(ForStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        if (!node.initializers().isEmpty())
            for (Object object : node.initializers())
                process((ASTNode) object, stmt, null, VARIABLE_TYPE_USE);
        if (node.getExpression() != null)
            process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        if (!node.updaters().isEmpty())
            for (Object object : node.updaters())
                process((ASTNode) object, stmt, null, VARIABLE_TYPE_USE);
        process(node.getBody(), null, null, VARIABLE_TYPE_NONE);
        return null;
    }

    /**
     * DoStatement:
     *	do Statement while ( Expression ) ;
     */
    private VarNode visit(DoStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        process(node.getBody(), null, null, VARIABLE_TYPE_NONE);
        return null;
    }

    /**
     * WhileStatement:
     *	while ( Expression ) Statement
     */
    private VarNode visit(WhileStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        process(node.getBody(), null, null, VARIABLE_TYPE_NONE);
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
    private VarNode visit(TryStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        if (node.resources() != null)
            for (Object object : node.resources())
                process((ASTNode) object, stmt, null, VARIABLE_TYPE_USE);
        process(node.getBody(), null, null, VARIABLE_TYPE_NONE);

        for (Object object : node.catchClauses()) {
            CatchClause catchClause = (CatchClause) object;
            process(catchClause, null, null, VARIABLE_TYPE_NONE);
        }

        if (node.getFinally() != null)
            process(node.getFinally(), null, null, VARIABLE_TYPE_NONE);
        return null;
    }

    /**
     * CatchClause
     *    catch ( SingleVariableDeclaration ) Block
     */
    private VarNode visit(CatchClause node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        process(node.getException(), stmt, null, VARIABLE_TYPE_DEF);
        process(node.getBody(), null, null, VARIABLE_TYPE_NONE);
        return null;
    }

    /**
     * ThrowStatement:
     *	throw Expression ;
     */
    private VarNode visit(ThrowStatement node, Stmt stmt, Assign assign, int type) {
        stmt = new Stmt(_inform, node, _unit.getLineNumber(node.getStartPosition()));
        _inform.addStatement(stmt);
        process(node.getExpression(), stmt, null, VARIABLE_TYPE_USE);
        return null;
    }

    /************************** Visit Expression ******************************/
    /**
     * Annotation:
     * NormalAnnotation
     * MarkerAnnotation
     * SingleMemberAnnotation
     */
    private VarNode visit(Annotation node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * ArrayAccess:
     * Expression [ Expression ]
     */
    private VarNode visit(ArrayAccess node, Stmt stmt, Assign assign, int type) {
        if (type == VARIABLE_TYPE_DEF) {
            process(node.getArray(), stmt, assign, VARIABLE_TYPE_DEF);
            process(node.getIndex(), stmt, assign, VARIABLE_TYPE_USE);
        } else {
            process(node.getArray(), stmt, assign, type);
            process(node.getIndex(), stmt, assign, type);
        }
        return null;
    }

    /**
     * ArrayCreation: new PrimitiveType [ Expression ] { [ Expression ] } { [ ]
     * } new TypeName [ < Type { , Type } > ] [ Expression ] { [ Expression ] }
     * { [ ] } new PrimitiveType [ ] { [ ] } ArrayInitializer new TypeName [ <
     * Type { , Type } > ] [ ] { [ ] } ArrayInitializer
     */
    private VarNode visit(ArrayCreation node, Stmt stmt, Assign assign, int type) {
        for (Object object : node.dimensions())
            process((ASTNode) object, stmt, assign, type);
        if (node.getInitializer() != null)
            process(node.getInitializer(), stmt, assign, type);
        return null;
    }

    /**
     * ArrayInitializer:
     *      { [ Expression { , Expression} [ , ]] }
     */
    private VarNode visit(ArrayInitializer node, Stmt stmt, Assign assign, int type) {
        for (Object object : node.expressions())
            process((ASTNode) object, stmt, assign, type);
        return null;
    }

    /**
     * CastExpression:
     * ( Type ) Expression
     */
    private VarNode visit(CastExpression node, Stmt stmt, Assign assign, int type) {
        process(node.getExpression(), stmt, assign, type);
        return null;
    }

    /**
     * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type
     * ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
     */
    private VarNode visit(ClassInstanceCreation node, Stmt stmt, Assign assign, int type) {
        if (node.getExpression() != null)
            process(node.getExpression(), stmt, assign, VARIABLE_TYPE_NONE);
        if (node.getAnonymousClassDeclaration() != null)
            process(node.getAnonymousClassDeclaration(), stmt, assign, VARIABLE_TYPE_NONE);
        for (Object object : node.arguments())
            process((ASTNode) object, stmt, assign, VARIABLE_TYPE_USE);
        return null;
    }

    private VarNode visit(AnonymousClassDeclaration node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * ConditionalExpression:
     *      Expression ? Expression : Expression
     */
    private VarNode visit(ConditionalExpression node, Stmt stmt, Assign assign, int type) {
        process(node.getExpression(), stmt, assign, type);
        process(node.getThenExpression(), stmt, assign, type);
        process(node.getElseExpression(), stmt, assign, type);
        return null;
    }

    /**
     * InfixExpression:
     *		Expression InfixOperator Expression { InfixOperator Expression }
     */
    private VarNode visit(InfixExpression node, Stmt stmt, Assign assign, int type) {
        process(node.getLeftOperand(), stmt, assign, type);
        process(node.getRightOperand(), stmt, assign, type);
        if (node.hasExtendedOperands())
            for (Object object : node.extendedOperands())
                process((Expression) object, stmt, assign, type);
        return null;
    }

    /**
     * InstanceofExpression:
     *		Expression instanceof Type
     */
    private VarNode visit(InstanceofExpression node, Stmt stmt, Assign assign, int type) {
        process(node.getLeftOperand(), stmt, assign, type);
        process(node.getRightOperand(), stmt, assign, VARIABLE_TYPE_NONE);
        return null;
    }

    /**
     * LambdaExpression:
     *	Identifier -> Body
     *	( [ Identifier { , Identifier } ] ) -> Body
     *	( [ FormalParameter { , FormalParameter } ] ) -> Body
     */
    private VarNode visit(LambdaExpression node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     *  MethodInvocation:
     *  [ Expression . ]
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
     */
    private VarNode visit(MethodInvocation node, Stmt stmt, Assign assign, int type) {
        process(node.getExpression(), stmt, assign, type);
        process(node.getName(), stmt, assign, VARIABLE_TYPE_NONE);
        for (Object object : node.arguments())
            process((ASTNode) object, stmt, assign, type);
        return null;
    }

    /**
     * ParenthesizedExpression:
     *	( Expression )
     */
    private VarNode visit(ParenthesizedExpression node, Stmt stmt, Assign assign, int type) {
        process(node.getExpression(), stmt, assign, type);
        return null;
    }

    /**
     * PostfixExpression:
     *	Expression PostfixOperator
     */
    private VarNode visit(PostfixExpression node, Stmt stmt, Assign assign, int type) {
        process(node.getOperand(), stmt, assign, type);
        return null;
    }

    /**
     * PrefixExpression:
     *	PrefixOperator Expression
     */
    private VarNode visit(PrefixExpression node, Stmt stmt, Assign assign, int type) {
        process(node.getOperand(), stmt, assign, type);
        return null;
    }

    /**
     * SuperMethodInvocation:
     *	[ ClassName . ] super .
     *    [ < Type { , Type } > ]
     *    Identifier ( [ Expression { , Expression } ] )
     */
    private VarNode visit(SuperMethodInvocation node, Stmt stmt, Assign assign, int type) {
        process(node.getName(), stmt, assign, VARIABLE_TYPE_NONE);
        if (node.getQualifier() != null)
            process(node.getQualifier(), stmt, assign, VARIABLE_TYPE_NONE);
        for (Object object : node.arguments())
            process((ASTNode) object, stmt, assign, type);
        return null;
    }

    /**
     * Assignment:
     *      Expression AssignmentOperator Expression
     */
    private VarNode visit(Assignment node, Stmt stmt, Assign assign, int type) {
        Assign assign1 = new Assign(stmt, _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        process(node.getLeftHandSide(), stmt, assign1, VARIABLE_TYPE_DEF);
        process(node.getRightHandSide(), stmt, assign1, VARIABLE_TYPE_USE);

        if ((assign != null) && (type == VARIABLE_TYPE_USE))
            assign.addUse(assign1.getDef());
        return null;
    }

    //-----------------------Declaration---------------------------
    /**
     * { ExtendedModifier } Type {Annotation} [ ... ] Identifier { Dimension } [ = Expression ]
     * "..." should not be appear since it is only used in method declarations
     */
    private VarNode visit(SingleVariableDeclaration node, Stmt stmt, Assign assign, int type) {
        assign = new Assign(stmt, _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        process(node.getName(), stmt, assign, VARIABLE_TYPE_DEF);
        if (node.getInitializer() != null)
            process(node.getInitializer(), stmt, assign, VARIABLE_TYPE_USE);
        return null;
    }

    /**
     * VariableDeclarationExpression:
     *	{ ExtendedModifier } Type VariableDeclarationFragment
     *	    { , VariableDeclarationFragment }
     */
    private VarNode visit(VariableDeclarationExpression node, Stmt stmt, Assign assign, int type) {
        for (Object object : node.fragments())
            process((ASTNode) object, stmt, assign, type);
        return null;
    }

    /**
     * VariableDeclarationFragment:
     *	Identifier { Dimension } [ = Expression ]
     */
    private VarNode visit(VariableDeclarationFragment node, Stmt stmt, Assign assign, int type) {
        assign = new Assign(stmt, _unit.getLineNumber(node.getStartPosition()), _unit.getColumnNumber(node.getStartPosition()));
        process(node.getName(), stmt, assign, VARIABLE_TYPE_DEF);
        if (node.getInitializer() != null)
            process(node.getInitializer(), stmt, assign, VARIABLE_TYPE_USE);
        return null;
    }

    //---------------------------Variable----------------------------
    /**
     * FieldAccess:
     *           Expression . Identifier
     */
    private VarNode visit(FieldAccess node, Stmt stmt, Assign assign, int type) {
        String varID = node.toString();
        if (type == VARIABLE_TYPE_NONE)
            return null;

        VarNode var = _inform.addVar(varID);
        if (type == VARIABLE_TYPE_DEF)
            assign.setDef(var);
        else if (type == VARIABLE_TYPE_USE) {
            stmt.addUse(var);
            if (assign != null)
                assign.addUse(var);
        }
        return null;
    }

    /**
     * Name:
     *	SimpleName
     *	QualifiedName
     */
    private VarNode visit(Name node, Stmt stmt, Assign assign, int type) {
        String varID = node.toString();
        if (type == VARIABLE_TYPE_NONE)
            return null;

        VarNode var = _inform.addVar(varID);
        if (type == VARIABLE_TYPE_DEF)
            assign.setDef(var);
        else if (type == VARIABLE_TYPE_USE) {
            stmt.addUse(var);
            if (assign != null)
                assign.addUse(var);
        }
        return null;
    }

    /**
     * SuperFieldAccess:
     *	[ ClassName . ] super . Identifier
     */
    private VarNode visit(SuperFieldAccess node, Stmt stmt, Assign assign, int type) {
        String varID = node.toString();
        if (type == VARIABLE_TYPE_NONE)
            return null;

        VarNode var = _inform.addVar(varID);
        if (type == VARIABLE_TYPE_DEF)
            assign.setDef(var);
        else if (type == VARIABLE_TYPE_USE) {
            stmt.addUse(var);
            if (assign != null)
                assign.addUse(var);
        }
        return null;
    }

    //-------------------------Literal--------------------------
    /**
     * BooleanLiteral:
     *      true false
     */
    private VarNode visit(BooleanLiteral node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * Character literal nodes.
     */
    private VarNode visit(CharacterLiteral node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * Null literal node.
     */
    private VarNode visit(NullLiteral node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * Number literal node.
     */
    private VarNode visit(NumberLiteral node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * String literal nodes.
     */
    private VarNode visit(StringLiteral node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * TypeLiteral:
     *	( Type | void ) . class
     */
    private VarNode visit(TypeLiteral node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * ThisExpression:
     *	[ ClassName . ] this
     */
    private VarNode visit(ThisExpression node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    //-------------------------Reference-------------------
    /**
     * CreationReference:
     *      Type ::
     *          [ < Type { , Type } > ]
     *      new
     */
    private VarNode visit(CreationReference node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * ExpressionMethodReference:
     *	Expression ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private VarNode visit(ExpressionMethodReference node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * MethodReference:
     *	CreationReference
     *	ExpressionMethodReference
     *	SuperMethodReference
     *	TypeMethodReference
     */
    private VarNode visit(MethodReference node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * SuperMethodReference:
     *	[ ClassName . ] super ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private VarNode visit(SuperMethodReference node, Stmt stmt, Assign assign, int type) {
        return null;
    }

    /**
     * TypeMethodReference:
     *	Type ::
     *	    [ < Type { , Type } > ]
     *	    Identifier
     */
    private VarNode visit(TypeMethodReference node, Stmt stmt, Assign assign, int type) {
        return null;
    }


    /************************** Process ******************************/
    TmpASTVisitor(CompilationUnit unit, String file) {
        _unit = unit;
        _file = file;
    }

    VarNode process(ASTNode node) {
        _inform = new SourceInform();
        process(node, null, null, VARIABLE_TYPE_NONE);
        _inform.buildEntryList();
        _inform.buildReturnList();
        return null;
    }

    private VarNode process(ASTNode node, Stmt stmt, Assign assign, int type) {
        if (node == null) {
            return null;
        }
        if (node instanceof AssertStatement) {
            return visit((AssertStatement) node, stmt, assign, type);
        } else if (node instanceof Block) {
            return visit((Block) node, stmt, assign, type);
        } else if (node instanceof BreakStatement) {
            return visit((BreakStatement) node, stmt, assign, type);
        } else if (node instanceof ConstructorInvocation) {
            return visit((ConstructorInvocation) node, stmt, assign, type);
        } else if (node instanceof ContinueStatement) {
            return visit((ContinueStatement) node, stmt, assign, type);
        } else if (node instanceof DoStatement) {
            return visit((DoStatement) node, stmt, assign, type);
        } else if (node instanceof EmptyStatement) {
            return visit((EmptyStatement) node, stmt, assign, type);
        } else if (node instanceof EnhancedForStatement) {
            return visit((EnhancedForStatement) node, stmt, assign, type);
        } else if (node instanceof ExpressionStatement) {
            return visit((ExpressionStatement) node, stmt, assign, type);
        } else if (node instanceof ForStatement) {
            return visit((ForStatement) node, stmt, assign, type);
        } else if (node instanceof IfStatement) {
            return visit((IfStatement) node, stmt, assign, type);
        } else if (node instanceof LabeledStatement) {
            return visit((LabeledStatement) node, stmt, assign, type);
        } else if (node instanceof ReturnStatement) {
            return visit((ReturnStatement) node, stmt, assign, type);
        } else if (node instanceof SuperConstructorInvocation) {
            return visit((SuperConstructorInvocation) node, stmt, assign, type);
        } else if (node instanceof SwitchCase) {
            return visit((SwitchCase) node, stmt, assign, type);
        } else if (node instanceof SwitchStatement) {
            return visit((SwitchStatement) node, stmt, assign, type);
        } else if (node instanceof SynchronizedStatement) {
            return visit((SynchronizedStatement) node, stmt, assign, type);
        } else if (node instanceof ThrowStatement) {
            return visit((ThrowStatement) node, stmt, assign, type);
        } else if (node instanceof TryStatement) {
            return visit((TryStatement) node, stmt, assign, type);
        } else if (node instanceof TypeDeclarationStatement) {
            return visit((TypeDeclarationStatement) node, stmt, assign, type);
        } else if (node instanceof VariableDeclarationStatement) {
            return visit((VariableDeclarationStatement) node, stmt, assign, type);
        } else if (node instanceof WhileStatement) {
            return visit((WhileStatement) node, stmt, assign, type);
        } else if (node instanceof Annotation) {
            return visit((Annotation) node, stmt, assign, type);
        } else if (node instanceof ArrayAccess) {
            return visit((ArrayAccess) node, stmt, assign, type);
        } else if (node instanceof ArrayCreation) {
            return visit((ArrayCreation) node, stmt, assign, type);
        } else if (node instanceof ArrayInitializer) {
            return visit((ArrayInitializer) node, stmt, assign, type);
        } else if (node instanceof Assignment) {
            return visit((Assignment) node, stmt, assign, type);
        } else if (node instanceof BooleanLiteral) {
            return visit((BooleanLiteral) node, stmt, assign, type);
        } else if (node instanceof CastExpression) {
            return visit((CastExpression) node, stmt, assign, type);
        } else if (node instanceof CharacterLiteral) {
            return visit((CharacterLiteral) node, stmt, assign, type);
        } else if (node instanceof ClassInstanceCreation) {
            return visit((ClassInstanceCreation) node, stmt, assign, type);
        } else if (node instanceof ConditionalExpression) {
            return visit((ConditionalExpression) node, stmt, assign, type);
        } else if (node instanceof CreationReference) {
            return visit((CreationReference) node, stmt, assign, type);
        } else if (node instanceof ExpressionMethodReference) {
            return visit((ExpressionMethodReference) node, stmt, assign, type);
        } else if (node instanceof FieldAccess) {
            return visit((FieldAccess) node, stmt, assign, type);
        } else if (node instanceof InfixExpression) {
            return visit((InfixExpression) node, stmt, assign, type);
        } else if (node instanceof InstanceofExpression) {
            return visit((InstanceofExpression) node, stmt, assign, type);
        } else if (node instanceof LambdaExpression) {
            return visit((LambdaExpression) node, stmt, assign, type);
        } else if (node instanceof MethodInvocation) {
            return visit((MethodInvocation) node, stmt, assign, type);
        } else if (node instanceof MethodReference) {
            return visit((MethodReference) node, stmt, assign, type);
        } else if (node instanceof Name) {
            return visit((Name) node, stmt, assign, type);
        } else if (node instanceof NullLiteral) {
            return visit((NullLiteral) node, stmt, assign, type);
        } else if (node instanceof NumberLiteral) {
            return visit((NumberLiteral) node, stmt, assign, type);
        } else if (node instanceof ParenthesizedExpression) {
            return visit((ParenthesizedExpression) node, stmt, assign, type);
        } else if (node instanceof PostfixExpression) {
            return visit((PostfixExpression) node, stmt, assign, type);
        } else if (node instanceof PrefixExpression) {
            return visit((PrefixExpression) node, stmt, assign, type);
        } else if (node instanceof StringLiteral) {
            return visit((StringLiteral) node, stmt, assign, type);
        } else if (node instanceof SuperFieldAccess) {
            return visit((SuperFieldAccess) node, stmt, assign, type);
        } else if (node instanceof SuperMethodInvocation) {
            return visit((SuperMethodInvocation) node, stmt, assign, type);
        } else if (node instanceof SuperMethodReference) {
            return visit((SuperMethodReference) node, stmt, assign, type);
        } else if (node instanceof ThisExpression) {
            return visit((ThisExpression) node, stmt, assign, type);
        } else if (node instanceof TypeLiteral) {
            return visit((TypeLiteral) node, stmt, assign, type);
        } else if (node instanceof TypeMethodReference) {
            return visit((TypeMethodReference) node, stmt, assign, type);
        } else if (node instanceof VariableDeclarationExpression) {
            return visit((VariableDeclarationExpression) node, stmt, assign, type);
        } else if (node instanceof AnonymousClassDeclaration) {
            return visit((AnonymousClassDeclaration) node, stmt, assign, type);
        } else if (node instanceof VariableDeclarationFragment) {
            return visit((VariableDeclarationFragment) node, stmt, assign, type);
        } else if (node instanceof SingleVariableDeclaration) {
            return visit((SingleVariableDeclaration) node, stmt, assign, type);
        } else if (node instanceof MethodDeclaration) {
            return visit((MethodDeclaration) node, stmt, assign, type);
        } else if (node instanceof CatchClause) {
            return visit((CatchClause) node, stmt, assign, type);
        } else {
            LevelLogger.error("UNKNOWN ASTNode type : " + node.toString());
            return null;
        }
    }

    public ArrayList<Stmt> getStatementList() {
        return _inform.getStatementList();
    }

    public ArrayList<VarNode> getReturnList() {
        return _inform.getReturnList();
    }

    public ArrayList<VarNode> getEntryList() {
        return _inform.getEntryList();
    }

    public String getSource() {
        return _inform.genFigaroSource();
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
}
