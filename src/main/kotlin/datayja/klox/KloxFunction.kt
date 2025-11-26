package datayja.klox

import kotlin.math.min

class KloxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment,
    private val isInitializer: Boolean,
) : KloxCallable {

    override val arity: Int = declaration.params.size

    override fun call(
        interpreter: Interpreter,
        arguments: List<Any?>
    ): Any? {
        require(arguments.size == arity)

        val environment = Environment(closure)

        for (i in 0..min(declaration.params.lastIndex, arguments.lastIndex)) {
            environment.define(
                name = declaration.params[i].lexeme,
                value = arguments[i],
            )
        }

        return try {
            interpreter.executeBlock(declaration.body, environment)
            if (isInitializer) {
                closure.getAt(0, "this")
            } else {
                null
            }
        } catch (returnValue: Interpreter.Return) {
            if (isInitializer) closure.getAt(0, "this")
            else returnValue.value
        }
    }

    fun bind(instance: KloxInstance): KloxFunction {
        return Environment(closure).let { env ->
            env.define("this", instance)
            KloxFunction(declaration = declaration, closure = env, isInitializer = isInitializer)
        }
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}
