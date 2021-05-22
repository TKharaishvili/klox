class LoxFunction(private val declaration: Stmt.Function,
                  private val closure: Environment,
                  private val isInitializer: Boolean
) : LoxCallable {
    override val arity: Int
        get() = declaration.params.size

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for (p in declaration.params.zip(arguments)) {
            environment.define(p.first.lexeme, p.second)
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            if (isInitializer) {
                return closure.getAt(0, "this")
            }

            return returnValue.value
        }

        if (isInitializer) {
            return closure.getAt(0, "this")
        }
        return null
    }

    override fun toString() = "<fn ${declaration.name.lexeme}>"
}