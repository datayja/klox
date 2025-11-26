package datayja.klox

import java.util.Stack

class Resolver(
    private val interpreter: Interpreter,
) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {

    private enum class FunctionType {
        None, Function, Method, Initializer
    }

    private enum class LoopType {
        None, While
    }

    private enum class ClassType {
        None, Class
    }

    private val scopes: Stack<MutableMap<String, Boolean>> = Stack()

    private var currentFunction: FunctionType = FunctionType.None

    private var currentLoop: LoopType = LoopType.None

    private var currentClass: ClassType = ClassType.None

    override fun visitAssignExpr(expr: Expr.Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visitBinaryExpr(expr: Expr.Binary) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitCallExpr(expr: Expr.Call) {
        resolve(expr.callee)

        for (argument in expr.arguments) {
            resolve(argument)
        }
    }

    override fun visitGetExpr(expr: Expr.Get) {
        resolve(expr.source)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping) {
        resolve(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal) {
        /* no-op */
    }

    override fun visitLogicalExpr(expr: Expr.Logical) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set) {
        resolve(expr.value)
        resolve(expr.destination)
    }

    override fun visitThisExpr(expr: Expr.This) {
        if (currentClass == ClassType.None) {
            Klox.error(expr.keyword, "Can't use 'this' outside of a class.")
            return
        }

        resolveLocal(expr, expr.keyword)
    }

    override fun visitUnaryExpr(expr: Expr.Unary) {
        resolve(expr.right)
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

    override fun visitClassStmt(stmt: Stmt.Class) {
        val enclosingClass = currentClass
        currentClass = ClassType.Class

        declare(stmt.name)
        define(stmt.name)

        beginScope()
        scopes.peek()["this"] = true

        for (method in stmt.methods) {
            var declaration = FunctionType.Method
            if (method.name.lexeme == "init") {
                declaration = FunctionType.Initializer
            }
            resolveFunction(method, declaration)
        }

        endScope()

        currentClass = enclosingClass
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        resolve(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt, FunctionType.Function)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        resolve(stmt.elseBranch)
    }

    override fun visitLoopControlStmt(stmt: Stmt.LoopControl) {
        if (currentLoop == LoopType.None) {
            Klox.error(stmt.type, "Can't ${stmt.type.lexeme} from outside of a loop.")
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        resolve(stmt.expression)
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        if (currentFunction == FunctionType.None) {
            Klox.error(stmt.keyword, "Can't return from top-level code.")
        }

        if (stmt.value != null) {
            if (currentFunction == FunctionType.Initializer) {
                Klox.error(stmt.keyword, "Can't return a value from an initializer.")
            }

            resolve(stmt.value)
        }
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        val enclosingLoop = currentLoop
        currentLoop = LoopType.While

        resolve(stmt.condition)
        resolve(stmt.body)

        currentLoop = enclosingLoop
    }

    internal fun resolve(statements: List<Stmt>) {
        statements.forEach(::resolve)
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

        if (scope.containsKey(name.lexeme)) {
            Klox.error(name, "Already a variable with this name is in this scope.")
        }

        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.peek()
        scope[name.lexeme] = true
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in scopes.lastIndex downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun resolveFunction(function: Stmt.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type

        beginScope()
        for (param in function.params) {
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()

        currentFunction = enclosingFunction
    }

}
