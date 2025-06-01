package datayja.klox

import arrow.core.raise.nullable

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {

    private var environment: Environment = Environment()

    fun interpret(statements: List<Stmt>) {
        try {
            statements.forEach { statement ->
                execute(statement)
            }
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
        return environment[expr.name]
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

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    internal fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment

            statements.forEach { statement ->
                execute(statement)
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
        }
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(value.stringify())
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = stmt.initializer?.let(::evaluate)

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }
}
