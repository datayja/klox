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
        "Binary   : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",
        "Literal  : Any? value",
        "Unary    : Token operator, Expr right",
        "Variable : Token name",
    ))
    defineAst(outputDir, "Stmt", listOf(
        "Expression : Expr expression",
        "Print      : Expr expression",
        "Var        : Token name, Expr? initializer",
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
    writer.println("  data class $className(")

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
