package datayja.klox

class KloxClass(
    val name: String,
) : KloxCallable {

    override fun call(
        interpreter: Interpreter,
        arguments: List<Any?>
    ): Any {
        val instance = KloxInstance(this)
        return instance
    }

    override val arity: Int
        get() = 0

    override fun toString(): String = name
}
