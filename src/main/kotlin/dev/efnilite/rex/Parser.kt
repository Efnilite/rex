package dev.efnilite.rex

import kotlin.reflect.full.memberFunctions

object Parser {

    /**
     * Parse the provided tokens into a list of objects.
     * @param tokens The tokens to parse.
     * @return The parsed list of objects.
     */
    fun parse(tokens: List<Token>): Any? {
        val scope = Scope(null)
        val list = mutableListOf<Any?>()

        for (token in tokens) {
            val parsed = parse(token)

            list += if (parsed is Fn) {
                parsed.invoke(scope)
            } else {
                parsed
            }
        }

        println(scope)

        // avoids returning ["hey"] instead of "hey" when single element is parsed
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

        return Fn(identifier.value, tokens.drop(1).map { parse(it) })
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

    override fun toString(): String {
        val entries = refs.entries.joinToString(", ") { "${it.key}: ${it.value}" }

        return "Scope $entries"
    }
}

class DefinedFn(
    private val params: Arr,
    private val body: List<Any?>
) {

    fun invoke(args: List<Any?>, scope: Scope): Any? {
        if (params.size != args.size) {
            throw IllegalArgumentException("Expected ${params.size} arguments, got ${args.size}")
        }

        for ((idx, arg) in args.withIndex()) {
            if (params[idx] !is String) {
                throw IllegalArgumentException("Expected a string, got ${params[idx]}")
            }

            scope.setReference(params[idx] as String, arg)
        }

        var result: Any? = null
        for (any in body) {
            if (any is Fn) {
                result = any.invoke(scope)
            }
        }

        return result
    }

    override fun toString(): String {
        return "DefinedFn(${params.joinToString(" ")} $body)"
    }
}

data class Fn(val identifier: String, val args: List<Any?>) {

    fun invoke(scope: Scope): Any? {
        return when {
            identifier.contains(".") && identifier.contains("/") -> {
                val (className, methodName) = identifier.split("/")

                val obj = Class.forName(className).kotlin.objectInstance!!
                val functions = obj.javaClass.kotlin.memberFunctions
                val method = functions.find { it.name == methodName }!!

                method.call(RT, *args.toTypedArray())
            }
            identifier == "def" -> {
                val name = args[0] as String
                val value = args[1]

                scope.setReference(name, value)

                name
            }
            identifier == "fn" -> {
                val params = args[0] as Arr
                val body = args.drop(1)

                DefinedFn(params, body)
            }
            else -> {
                val ref = scope.getReference(identifier)

                if (ref is Fn) {
                    return ref.invoke(Scope(scope))
                }

                null
            }
        }
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

    val size get() = values.size

    operator fun get(index: Int): Any? {
        return values[index]
    }

    fun joinToString(separator: String): String {
        return values.joinToString(separator)
    }

    override fun toString(): String {
        return "[${values.joinToString(" ")}]"
    }
}