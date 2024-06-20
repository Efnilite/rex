package dev.efnilite.rex

import kotlin.reflect.full.memberFunctions

object Parser {

    /**
     * Parse the provided tokens into a list of objects.
     * @param tokens The tokens to parse.
     * @return The parsed list of objects.
     */
    fun parse(tokens: List<Token>): Any? {
        val list = mutableListOf<Any?>()

        for (token in tokens) {
            val parsed = parse(token)

            list += if (parsed is Fn) {
                parsed.invoke()
            } else {
                parsed
            }
        }

        return if (list.size == 1) list[0] else list
    }

    private fun parse(token: Token): Any? {
        return when (token) {
            is FnToken -> parseFn(token)
            is MapToken -> parseMap(token)
            is ArrToken -> parseArr(token)
            is Literal<*> -> token.value
            else -> error("Invalid token")
        }
    }

    private fun parseFn(fn: FnToken): Fn {
        val tokens = fn.tokens
        val identifier = tokens[0] as IdentifierToken

        return Fn(identifier.value, tokens.drop(1).map { parse(it) }, Scope(null))
    }

    private fun parseMap(mp: MapToken): Mp {
        val map = mutableMapOf<Any?, Any?>()

        for (pair in mp.tokens.chunked(2)) {
            if (pair.size % 2 != 0) {
                error("Map must have an even number of elements")
            }

            map[parse(pair[0])] = parse(pair[1])
        }

        return Mp(map)
    }

    private fun parseArr(arr: ArrToken): Arr {
        val list = mutableListOf<Any?>()

        for (token in arr.tokens) {
            list += parse(token)
        }

        return Arr(list)
    }

    private fun error(message: String) {
        throw IllegalArgumentException(message)
    }
}

class Scope(private val parent: Scope? = null) {

    private val refs = mutableMapOf<String, Any?>()

    fun setReference(name: String, value: Any?) {
        refs[name] = value
    }

    fun getReference(name: String): Any? {
        if (refs.containsKey(name)) {
            return refs[name]
        }

        return parent?.getReference(name)
    }
}

class DefinedFn(private val params: List<String>,
                private val body: List<Any?>,
                private val scope: Scope
) {

    fun invoke(args: List<Any?>): Any? {
        if (params.size != args.size) {
            throw IllegalArgumentException("Expected ${params.size} arguments, got ${args.size}")
        }

        for ((idx, arg) in args.withIndex()) {
            scope.setReference(params[idx], arg)
        }

        var result: Any? = null
        for (any in body) {
            if (any is Fn) {
                result = any.invoke()
            }
        }

        return result
    }

    override fun toString(): String {
        return "DefinedFn(${params.joinToString(" ")} $body)"
    }
}

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

data class Mp(val elements: Map<Any?, Any?>) {

    override fun toString(): String {
        val entries = elements.entries.joinToString(", ") { "${it.key}: ${it.value}" }

        return "{$entries}"
    }
}

data class Arr(val values: List<Any?>) {

    override fun toString(): String {
        return "[${values.joinToString(" ")}]"
    }
}