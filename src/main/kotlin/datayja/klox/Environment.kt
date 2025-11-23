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

    internal operator fun get(name: Token): Any? {
        return synchronized(values) {
            if (values.containsKey(name.lexeme)) {
                values[name.lexeme]
            } else if (enclosing != null) {
                enclosing[name]
            } else {
                throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
            }
        }
    }

    internal fun assign(name: Token, value: Any?) {
        return synchronized(values) {
            if (values.containsKey(name.lexeme)) {
                values[name.lexeme] = value
            } else if (enclosing != null) {
                enclosing.assign(name, value)
            } else {
                throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
            }
        }
    }
}
