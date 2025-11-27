package datayja.klox

class KloxMetaclass(
    name: String,
    methods: Map<String, KloxFunction>,
    classMethods: Map<String, KloxFunction>,
) : KloxClass(name, classMethods) {

    val instanceMethods = methods

    override fun call(interpreter: Interpreter, arguments: List<Any?>): KloxClass {
        require(arguments.isEmpty()) { "Class.init can't have any arguments." }

        val instance = KloxClass(name, instanceMethods)

        instance.kloxClass = this

        findMethod("init")
            ?.bind(instance)
            ?.call(interpreter, arguments)

        return instance
    }

    override fun toString(): String {
        return super.toString() + "Class"
    }
}
