package datayja.klox

import java.util.Deque
import java.util.LinkedList
import java.util.Stack

class Resolver(
    private val interpreter: Interpreter,
) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {

    private val scopes: Stack<MutableMap<String, Boolean>> = Stack()

    private fun resolve(statements: List<Stmt>) {
        statements.forEach(::resolve)
    }

    override fun visitAssignExpr(expr: Expr.Assign) {
        TODO("Not yet implemented")
    }

    override fun visitBinaryExpr(expr: Expr.Binary) {
        TODO("Not yet implemented")
    }

    override fun visitCallExpr(expr: Expr.Call) {
        TODO("Not yet implemented")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping) {
        TODO("Not yet implemented")
    }

    override fun visitLiteralExpr(expr: Expr.Literal) {
        TODO("Not yet implemented")
    }

    override fun visitLogicalExpr(expr: Expr.Logical) {
        TODO("Not yet implemented")
    }

    override fun visitUnaryExpr(expr: Expr.Unary) {
        TODO("Not yet implemented")
    }

    override fun visitVariableExpr(expr: Expr.Variable) {
        if (scopes.isNotEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            Klox.error(expr.name, "Can't read local variable in its own initializer.")
        }

        resolveLocal(expr, expr.name)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    private fun resolve(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.peek()
        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.peek()
        scope[name.lexeme] = true
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in (scopes.size - 1) downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        TODO("Not yet implemented")
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        TODO("Not yet implemented")
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        TODO("Not yet implemented")
    }

    override fun visitLoopControlStmt(stmt: Stmt.LoopControl) {
        TODO("Not yet implemented")
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        TODO("Not yet implemented")
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        TODO("Not yet implemented")
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        TODO("Not yet implemented")
    }
}
