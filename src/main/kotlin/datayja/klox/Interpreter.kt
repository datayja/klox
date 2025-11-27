package datayja.klox

import arrow.core.raise.nullable
import java.io.Serial
import java.time.Clock
import java.time.Instant

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {

    val globals: Environment = Environment()
    private var environment: Environment = globals

    private val locals: MutableMap<Expr, Int> = mutableMapOf()

    private var isInLoop: Boolean = false

    constructor() {
        globals.define("clock", object : KloxCallable {
            override val arity: Int
                get() = 0

            override fun call(
                interpreter: Interpreter,
                arguments: List<Any?>
            ): Any = (Instant.now(Clock.systemDefaultZone()).toEpochMilli() / 1000.0)

            override fun toString(): String = "<native fn>"
        })
    }

    fun interpret(statements: List<Stmt>) {
        try {
            statements.forEach { statement ->
                execute(statement)
            }
        } catch (error: RuntimeError) {
            Klox.runtimeError(error)
        } catch (error: Throwable) {
            System.err.println(error)
            error.printStackTrace(System.err)
        }
    }

    private fun Any?.stringify(): String {
        return when (this) {
            null -> "nil"
            is Double -> this.toString().removeSuffix(".0")
            else -> this.toString()
        }
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        return when {
            expr.operator.type == TokenType.OR && left.isTruthy() -> left
            expr.operator.type == TokenType.AND && !left.isTruthy() -> left
            else -> evaluate(expr.right)
        }
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val destination = evaluate(expr.destination)

        if (destination !is KloxInstance) {
            throw RuntimeError(expr.name, "Only instances have fields.")
        }

        val value = evaluate(expr.value)
        destination[expr.name] = value

        return value
    }

    override fun visitThisExpr(expr: Expr.This): Any? {
        return lookUpVariable(expr.keyword, expr)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.BANG -> !right.isTruthy()
            TokenType.MINUS -> -(right as Double)
            else -> null
        }
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return lookUpVariable(expr.name, expr)
    }

    private fun lookUpVariable(name: Token, expr: Expr): Any?  {
        val distance = locals[expr]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals[name]
        }
    }

    private fun Any?.isTruthy(): Boolean {
        return when (this) {
            null -> false
            false -> false
            else -> true
        }
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        val superclass: KloxClass? = if (stmt.superclass != null) {
            evaluate(stmt.superclass)
                .also { sc ->
                    if (sc !is KloxClass) {
                        throw RuntimeError(stmt.superclass.name, "Superclass must be a class.")
                    }
                }
                .let { it as KloxClass }
        } else {
            null
        }

        environment.define(stmt.name.lexeme, null)

        val methods = buildMap {
            for (method in stmt.methods) {
                val function = KloxFunction(declaration = method, closure = environment, isInitializer = method.name.lexeme == "init")
                put(method.name.lexeme, function)
            }
        }
        val classMethods = buildMap {
            for (method in stmt.classMethods) {
                val function = KloxFunction(declaration = method, closure = environment, isInitializer = method.name.lexeme == "init")
                put(method.name.lexeme, function)
            }
        }

        val kloxMetaclass = KloxMetaclass(stmt.name.lexeme, superclass, methods, classMethods)
        val kloxClass = kloxMetaclass.call(this, emptyList())
        environment.assign(stmt.name, kloxClass)
    }

    internal fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment

            statements.forEach { statement ->
                execute(statement)
            }
        } catch (brk: Break) {
            if (this.isInLoop) {
                throw brk
            } else {
                throw RuntimeException("Unexpected break", brk)
            }
        } catch (cont: Continue) {
            if (this.isInLoop) {
                throw cont
            } else {
                throw RuntimeException("Unexpected continue", cont)
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        return when (expr.operator.type) {
            TokenType.LEFT_PAREN -> TODO()
            TokenType.RIGHT_PAREN -> TODO()
            TokenType.LEFT_BRACE -> TODO()
            TokenType.RIGHT_BRACE -> TODO()
            TokenType.COMMA -> TODO()
            TokenType.DOT -> TODO()
            TokenType.MINUS -> nullable {
                val left = (evaluate(expr.left) as? Double).bind()
                val right = (evaluate(expr.right) as? Double).bind()
                (left - right)
            }
            TokenType.PLUS -> nullable {
                val left = evaluate(expr.left).bind()
                val right = evaluate(expr.right).bind()
                if (left is Double && right is Double) {
                    (left + right)
                } else {
                    (left.toString() + right.toString())
                }
            }
            TokenType.SEMICOLON -> TODO()
            TokenType.SLASH -> nullable {
                val left = (evaluate(expr.left) as? Double).bind()
                val right = (evaluate(expr.right) as? Double)?.takeIf { it != 0.0 }.bind()
                (left / right)
            }
            TokenType.STAR -> nullable {
                val left = (evaluate(expr.left) as? Double).bind()
                val right = (evaluate(expr.right) as? Double).bind()
                (left * right)
            }
            TokenType.BANG -> TODO()
            TokenType.BANG_EQUAL -> {
                val left = evaluate(expr.left)
                val right = evaluate(expr.right)
                // Kotlin already has the right semantics for [!=]
                (left != right)
            }
            TokenType.EQUAL -> TODO()
            TokenType.EQUAL_EQUAL -> {
                val left = evaluate(expr.left)
                val right = evaluate(expr.right)
                // Kotlin already has the right semantics for [==]
                (left == right)
            }
            TokenType.GREATER -> nullable {
                val left = (evaluate(expr.left) as? Double).bind()
                val right = (evaluate(expr.right) as? Double).bind()
                (left > right)
            }
            TokenType.GREATER_EQUAL -> nullable {
                val left = (evaluate(expr.left) as? Double).bind()
                val right = (evaluate(expr.right) as? Double).bind()
                (left >= right)
            }
            TokenType.LESS -> nullable {
                val left = (evaluate(expr.left) as? Double).bind()
                val right = (evaluate(expr.right) as? Double).bind()
                (left < right)
            }
            TokenType.LESS_EQUAL -> nullable {
                val left = (evaluate(expr.left) as? Double).bind()
                val right = (evaluate(expr.right) as? Double).bind()
                (left <= right)
            }
            TokenType.IDENTIFIER -> TODO()
            TokenType.STRING -> TODO()
            TokenType.NUMBER -> TODO()
            TokenType.AND -> TODO()
            TokenType.CLASS -> TODO()
            TokenType.ELSE -> TODO()
            TokenType.FALSE -> TODO()
            TokenType.FUN -> TODO()
            TokenType.FOR -> TODO()
            TokenType.IF -> TODO()
            TokenType.NIL -> TODO()
            TokenType.OR -> TODO()
            TokenType.PRINT -> TODO()
            TokenType.RETURN -> TODO()
            TokenType.SUPER -> TODO()
            TokenType.THIS -> TODO()
            TokenType.TRUE -> TODO()
            TokenType.VAR -> TODO()
            TokenType.WHILE -> TODO()
            TokenType.EOF -> TODO()
            TokenType.BREAK -> TODO()
            TokenType.CONTINUE -> TODO()
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments = expr.arguments.map(::evaluate)

        if (callee !is KloxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes, got $callee")
        }

        if (arguments.size != callee.arity) {
            throw RuntimeError(expr.paren, "Expected ${callee.arity} arguments, but got ${arguments.size}")
        }

        return callee.call(this@Interpreter, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val source = evaluate(expr.source)
        if (source is KloxInstance) {
            return source[expr.name]
        } else {
            throw RuntimeError(expr.name, "Only instances have properties.")
        }
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = KloxFunction(declaration = stmt, closure = environment, isInitializer = false)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (evaluate(stmt.condition).isTruthy()) {
            execute(stmt.thenBranch)
        } else {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(value.stringify())
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        val value = nullable {
            evaluate(stmt.value.bind())
        }

        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = stmt.initializer?.let(::evaluate)

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        val previous = this.isInLoop
        try {
            this.isInLoop = true
            while (evaluate(stmt.condition).isTruthy()) {
                try {
                    execute(stmt.body)
                } catch (brk: Break) {
                    break
                } catch (cont: Continue) {
                    continue
                }
            }
        } finally {
            this.isInLoop = previous
        }
    }

    override fun visitLoopControlStmt(stmt: Stmt.LoopControl) {
        when (stmt.type.type) {
            TokenType.BREAK -> throw Break()
            TokenType.CONTINUE -> throw Continue()
            else -> throw RuntimeException("Illegal loop control $stmt")
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)

        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }

        return value
    }

    class Break : Throwable("break", null, true, false) {
        companion object {
            @Serial
            private const val serialVersionUID: Long = -4795569792694658446L
        }
    }

    class Continue : Throwable("continue", null, true, false) {
        companion object {
            @Serial
            private const val serialVersionUID: Long = 8160767980093585358L
        }
    }

    class Return(val value: Any?) : Throwable("return", null, true, false) {
        companion object {
            @Serial
            private const val serialVersionUID: Long = -1373715850196995391L
        }
    }
}
