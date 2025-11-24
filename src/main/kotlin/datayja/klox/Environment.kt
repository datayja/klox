package datayja.klox

class Environment(
    val enclosing: Environment? = null,
) {

    private val values: MutableMap<String, Any?> = mutableMapOf()

    internal fun define(name: String, value: Any?) {
        synchronized(values) {
            values[name] = value
        }
    }

    internal fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance).values[name]
    }

    internal fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    internal fun ancestor(distance: Int): Environment {
        var environment: Environment = this
        repeat(distance - 1) {
            environment = environment.enclosing!!
        }

        return environment
    }

    internal operator fun get(name: Token): Any? {
        return if (values.containsKey(name.lexeme)) {
            values[name.lexeme]
        } else if (enclosing != null) {
            enclosing[name]
        } else {
            throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }

    internal fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
        } else if (enclosing != null) {
            enclosing.assign(name, value)
        } else {
            throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }
}
