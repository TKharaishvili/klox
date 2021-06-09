class LoxClass(
    val name: String,
    private val superclass: LoxClass?,
    private val methods: Map<String, LoxFunction>
) : LoxCallable {
    override val arity get() = initializer?.arity ?: 0

    fun findMethod(name: String): LoxFunction? = methods[name] ?: superclass?.findMethod(name)

    override fun call(interpreter: Interpreter, arguments: List<Any?>): LoxInstance {
        val instance = LoxInstance(this)
        initializer?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    private val initializer get() = findMethod("init")

    override fun toString() = name
}