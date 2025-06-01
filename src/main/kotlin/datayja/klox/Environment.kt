package datayja.klox

class Environment {

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
            } else {
                throw RuntimeException("Undefined variable '${name.lexeme}'.")
            }
        }
    }
}
