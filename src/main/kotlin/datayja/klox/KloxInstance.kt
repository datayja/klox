package datayja.klox

class KloxInstance(
    val kloxClass: KloxClass,
) {

    private val fields: MutableMap<String, Any?> = mutableMapOf()

    operator fun get(name: Token): Any? {
        return when {
            fields.containsKey(name.lexeme) -> {
                fields[name.lexeme]
            }
            kloxClass.hasMethod(name.lexeme) -> {
                kloxClass.findMethod(name.lexeme)!!.bind(this)
            }
            else -> {
                throw RuntimeError(name, "Undefined property '$kloxClass.${name.lexeme}'.")
            }
        }
    }

    operator fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return "$kloxClass instance ${Integer.toHexString(System.identityHashCode(this))}"
    }
}
