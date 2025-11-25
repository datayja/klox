package datayja.klox

class KloxInstance(
    val kloxClass: KloxClass,
) {

    private val fields: MutableMap<String, Any?> = mutableMapOf()

    operator fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        } else {
            throw RuntimeError(name, "Undefined property '$kloxClass.${name.lexeme}'.")
        }
    }

    operator fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return "$kloxClass instance ${Integer.toHexString(System.identityHashCode(this))}"
    }
}
