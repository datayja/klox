package datayja.klox

// TODO: make KloxClass extend KloxInstance, add support for class methods and static variables

open class KloxClass(
    val name: String,
    val superclass: KloxClass?,
    val methods: Map<String, KloxFunction>,
) : KloxInstance(null), KloxCallable {

    override fun call(
        interpreter: Interpreter,
        arguments: List<Any?>
    ): KloxInstance {
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

    fun hasMethod(name: String): Boolean {
        return methods.containsKey(name) ||
                (superclass?.hasMethod(name) == true)
    }

    fun findMethod(name: String): KloxFunction? {
        return if (methods.containsKey(name)) {
            methods[name]
        } else {
            superclass?.findMethod(name)
        }
    }

    override fun toString(): String = name
}
