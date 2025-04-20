package datayja.klox

class Interpreter : Expr.Visitor<Any?> {

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

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        TODO("Not yet implemented")
    }
}
