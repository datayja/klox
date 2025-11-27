package datayja.tool

import java.io.IOException
import java.io.PrintWriter
import java.util.Locale
import kotlin.system.exitProcess

@Throws(IOException::class)
fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output directory>")
        exitProcess(64)
    }
    val outputDir = args[0]
    defineAst(outputDir, "Expr", listOf(
        "Assign   : Token name, Expr value",
        "Binary   : Expr left, Token operator, Expr right",
        "Call     : Expr callee, Token paren, List<Expr> arguments",
        "Get      : Expr source, Token name",
        "Grouping : Expr expression",
        "Literal  : Any? value",
        "Logical  : Expr left, Token operator, Expr right",
        "Set      : Expr destination, Token name, Expr value",
        "This     : Token keyword",
        "Unary    : Token operator, Expr right",
        "Variable : Token name",
    ))
    defineAst(outputDir, "Stmt", listOf(
        "Block       : List<Stmt> statements",
        "Class       : Token name, List<Stmt.Function> methods, List<Stmt.Function> classMethods",
        "Expression  : Expr expression",
        "Function    : Token name, List<Token> params, List<Stmt> body",
        "If          : Expr condition, Stmt thenBranch, Stmt elseBranch",
        "LoopControl : Token type",
        "Print       : Expr expression",
        "Return      : Token keyword, Expr? value",
        "Var         : Token name, Expr? initializer",
        "While       : Expr condition, Stmt body",
    ))
}

@Throws(IOException::class)
private fun defineAst(
    outputDir: String,
    baseName: String,
    types: List<String>,
) {
    val path = "$outputDir/$baseName.kt"
    PrintWriter(path, Charsets.UTF_8).use { writer ->
        writer.println("package datayja.klox")
        writer.println()
        writer.println("sealed class $baseName {")

        defineVisitor(writer, baseName, types)

        // the AST classes
        for (type in types) {
            val (className, fields) = type.split(":").map { it.trim() }
            defineType(writer, baseName, className, fields)
        }

        // the base accept() method
        writer.println("  abstract fun <R> accept(visitor: Visitor<R>): R")

        writer.println("}")
        writer.println()
        writer.flush()
    }
}

@Throws(IOException::class)
private fun defineVisitor(
    writer: PrintWriter,
    baseName: String,
    types: List<String>,
) {
    writer.println("  interface Visitor<R> {")

    for (type in types) {
        val typeName = type.split(":")[0].trim()
        writer.println("    fun visit$typeName$baseName(${baseName.lowercase(Locale.ROOT)}: $typeName): R")
    }

    writer.println("  }")
    writer.println()
}

@Throws(IOException::class)
private fun defineType(
    writer: PrintWriter,
    baseName: String,
    className: String,
    fieldList: String,
) {
    writer.println("  class $className(")

    // constructor fields
    val fields = fieldList.split(", ")
    for (field in fields) {
        val (type, name) = field.split(" ")
        writer.println("    val $name: $type,")
    }

    writer.println("  ) : $baseName() {")
    writer.println("    override fun <R> accept(visitor: Visitor<R>): R {")
    writer.println("      return visitor.visit$className$baseName(this)")
    writer.println("    }")
    writer.println("  }")
    writer.println()
}
