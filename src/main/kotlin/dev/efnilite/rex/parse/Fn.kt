package dev.efnilite.rex.parse

data class Fn(val identifier: String, val args: List<Any?>, val scope: Scope) {

    fun invoke(passed: List<Any?>) {
        val ref = scope.getReference(identifier)

        if (ref is Fn) {
            ref.invoke(args)
        }
        // todo this
    }

    override fun toString(): String {
        return "($identifier ${args.joinToString(" ")})"
    }
}