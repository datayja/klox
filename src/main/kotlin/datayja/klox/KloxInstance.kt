package datayja.klox

open class KloxInstance(
    var kloxClass: KloxClass?,
) {

    private val fields: MutableMap<String, Any?> = mutableMapOf()

    operator fun get(name: Token): Any? {
        return when {
            fields.containsKey(name.lexeme) -> {
                fields[name.lexeme]
            }
            kloxClass?.hasMethod(name.lexeme) == true -> {
                kloxClass!!.findMethod(name.lexeme)!!.bind(this)
            }
            else -> {
                throw RuntimeError(name, "Undefined property '${displayClassName()}.${name.lexeme}'.")
            }
        }
    }

    operator fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return "${displayClassName()} instance ${Integer.toHexString(System.identityHashCode(this))}"
    }

    private fun displayClassName(): String {
        return kloxClass?.toString() ?: "KloxMetaclass"
    }
}
