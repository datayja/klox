package datayja.klox

import kotlin.math.min

class KloxFunction(
    val declaration: Stmt.Function,
) : KloxCallable {

    override val arity: Int = declaration.params.size

    override fun call(
        interpreter: Interpreter,
        arguments: List<Any?>
    ): Any? {
        require(arguments.size == arity)

        val environment = Environment(interpreter.globals)

        for (i in 0..min(declaration.params.lastIndex, arguments.lastIndex)) {
            environment.define(
                name = declaration.params[i].lexeme,
                value = arguments[i],
            )
        }

        return try {
            interpreter.executeBlock(declaration.body, environment)
            null
        } catch (returnValue: Interpreter.Return) {
            returnValue.value
        }
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}
