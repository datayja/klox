package datayja.klox

// TODO: make KloxClass extend KloxInstance, add support for class methods and static variables

class KloxClass(
    val name: String,
    val methods: Map<String, KloxFunction>,
) : KloxCallable {

    override fun call(
        interpreter: Interpreter,
        arguments: List<Any?>
    ): Any {
        val instance = KloxInstance(this)

        findMethod("init")
            ?.bind(instance)
            ?.call(interpreter, arguments)

        return instance
    }

    override val arity: Int
        get() {
            return findMethod("init")
                ?.arity
                ?: 0
        }

    fun hasMethod(name: String): Boolean = methods.containsKey(name)

    fun findMethod(name: String): KloxFunction? = methods[name]

    override fun toString(): String = name
}
