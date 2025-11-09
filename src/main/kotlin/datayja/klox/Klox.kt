package datayja.klox

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


@Throws(IOException::class)
fun main(args: Array<String>) {
    Klox.main(args)
}

object Klox {

    private val interpreter: Interpreter = Interpreter()

    @Throws(IOException::class)
    fun main(args: Array<String>) {
        when (args.size) {
            0 -> runPrompt()
            1 -> runFile(args[0])
            else -> {
                System.err.println("Usage: klox [script]")
                exitProcess(64)
            }
        }
    }

    @Throws(IOException::class)
    private fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        runKlox(String(bytes, Charset.defaultCharset()))
        if (hadError) exitProcess(65)
    }

    @Throws(IOException::class)
    private fun runPrompt() {
        InputStreamReader(System.`in`).use { input ->
            BufferedReader(input).use { reader ->
                while (true) {
                    print("> ")
                    val line = reader.readLine()
                    if (line == null) break
                    else runKlox(line)
                    hadError = false
                }
            }
        }
    }

    private fun runKlox(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val statements = parser.parse()

        // stop if there was a syntax error.
        if (hadError || statements == null) return

        //println(AstPrinter().print(expression))
        interpreter.interpret(statements)
    }

    fun error(line: UInt, message: String) {
        report(line, "", message)
    }

    var hadError: Boolean = false

    private fun report(line: UInt, where: String, message: String) {
        System.err.println(
            "[line $line] Error$where: $message"
        )
        hadError = true
    }

    fun error(token: Token, message: String) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '${token.lexeme}'", message)
        }
    }
}
