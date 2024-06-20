package dev.efnilite.rex.parse

import dev.efnilite.rex.RT
import kotlin.reflect.full.memberFunctions

data class Fn(val identifier: String, val args: List<Any?>, val scope: Scope) {

    fun invoke(): Any? {
        if (identifier.contains(".")) {
            val (className, methodName) = identifier.split("/")

            val obj = Class.forName(className).kotlin.objectInstance!!
            val functions = obj.javaClass.kotlin.memberFunctions
            val method = functions.find { it.name == methodName }!!

            return method.call(RT, *args.toTypedArray())
        }

        val ref = scope.getReference(identifier)

        if (ref is Fn) {
            return ref.invoke()
        }

        return null
    }

    override fun toString(): String {
        return "ReferencedFn($identifier ${args.joinToString(" ")})"
    }
}