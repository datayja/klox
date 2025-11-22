package datayja.klox

import java.io.IOException

class AstPrinter : Expr.Visitor<String> {
    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        return parenthesize(
            name = "assign to '${expr.name}'",
            expr.value,
        )
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return parenthesize(
            name = expr.operator.lexeme,
            expr.left,
            expr.right,
        )
    }

    override fun visitCallExpr(expr: Expr.Call): String {
        TODO("Not yet implemented")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return parenthesize(
            name = "group",
            expr.expression,
        )
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return if (expr.value == null)
            "nil"
        else
            expr.value.toString()
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        TODO("Not yet implemented")
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(
            name = expr.operator.lexeme,
            expr.right,
        )
    }

    override fun visitVariableExpr(expr: Expr.Variable): String {
        return parenthesize(
            name = "variable",
            expr
        )
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        return buildString {
            append("(")
            append(name)
            for (expr in exprs) {
                append(" ")
                append(expr.accept(this@AstPrinter))
            }
            append(")")
        }
    }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
    val expression = Expr.Binary(
        left = Expr.Unary(
            operator = Token(TokenType.MINUS, "-", null, 1u),
            right = Expr.Literal(123),
        ),
        operator = Token(TokenType.STAR, "*", null, 1u),
        right = Expr.Grouping(
            Expr.Literal(45.67)
        ),
    )

    println(AstPrinter().print(expression))
}
