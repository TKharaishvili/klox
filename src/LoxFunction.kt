class LoxFunction(private val declaration: Stmt.Function,
                  private val closure: Environment
) : LoxCallable {
    override val arity: Int
        get() = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for (p in declaration.params.zip(arguments)) {
            environment.define(p.first.lexeme, p.second)
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun toString() = "<fn ${declaration.name.lexeme}>"
}