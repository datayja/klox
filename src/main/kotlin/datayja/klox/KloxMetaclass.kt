package datayja.klox

class KloxMetaclass(
    name: String,
    superclass: KloxClass?,
    methods: Map<String, KloxFunction>,
    classMethods: Map<String, KloxFunction>,
) : KloxClass(name, (superclass?.kloxClass as? KloxMetaclass), classMethods) {

    init {
        if (superclass != null) {
            require(superclass.kloxClass is KloxMetaclass) { "Class of '$superclass' has to be an instance of KloxMetaclass" }
        }
    }

    val instanceMethods = methods

    val instanceSuperclass = superclass

    override fun call(interpreter: Interpreter, arguments: List<Any?>): KloxClass {
        require(arguments.isEmpty()) { "Class.init can't have any arguments." }

        val instance = KloxClass(name, instanceSuperclass, instanceMethods)

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
