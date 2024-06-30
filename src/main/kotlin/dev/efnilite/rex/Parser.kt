package dev.efnilite.rex

import kotlin.reflect.full.memberFunctions

object Parser {

    /**
     * Parse the provided tokens into a list of objects.
     *
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

    private fun parseFn(fn: FnToken): Any {
        val tokens = fn.tokens
        val identifier = tokens[0]

        return if (identifier is IdentifierToken && identifier.value == "fn") {
            AnonymousFn(parse(tokens[1]) as Arr, tokens.drop(2).map { parse(it) })
        } else {
            Fn(parse(identifier), tokens.drop(1).map { parse(it) })
        }
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
        val entries = refs.entries.joinToString("\n") { "${it.key}: ${it.value}" }

        return "Scope {\n$entries\n}"
    }
}

/**
 * Represents an anonymous function.
 *
 * These have `fn` as identifier, with used params as a possibly empty [Arr] and a body to evaluate on invocation.
 * Return value is the last evaluated expression in the body.
 *
 * @param params An array with the params of the fn.
 * @param body The expressions in the fn that act as the body.
 * @constructor Creates an anonymous function with the provided parameters and body.
 */
class AnonymousFn(
    private val params: Arr,
    private val body: List<Any?>
) {

    /**
     * Invokes this anonymous function with the provided arguments.
     *
     * Replaces the params with the provided arguments in the scope and evaluates the body.
     *
     * @param args The arguments to replace the params with.
     * @param scope The scope to evaluate the body in. Contains the parent scope.
     * @return The result of the last evaluated expression in the body.
     * @throws IllegalArgumentException If the number of arguments does not match the number of params.
     * @throws IllegalArgumentException If a param is not a string.
     */
    fun invoke(args: List<Any?>, scope: Scope): Any? {
        if (params.size != args.size) {
            error("Expected ${params.size} arguments, got ${args.size}")
        }

        for ((idx, arg) in args.withIndex()) {
            if (params[idx] !is String) {
                error("Expected a string, got ${params[idx]}")
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
        return "AnonymousFn(${params.joinToString(" ")} $body)"
    }
}

/**
 * Represents an S-expression.
 *
 * These have an identifier (the first argument) and a list of remaining arguments.
 *
 * @param identifier The identifier of the function.
 * @param args The arguments to pass to the function.
 * @constructor Creates an S-expression with the provided identifier and arguments.
 */
data class Fn(val identifier: Any?, val args: List<Any?>) {

    /**
     * Invokes this S-expression with the provided scope.
     *
     * - If the identifier is a different S-expression, invokes it first.
     * - If the identifier is an anonymous function, invokes it with the provided arguments.
     * - If the identifier is a reference to an existing class and method, invoke it with arguments.
     * - If the identifier is for setting a variable, set the variable to the value of the second argument.
     * - If the identifier is a reference to an existing function, invoke it.
     *
     * @param scope The scope to evaluate the function in. Contains the parent scope.
     * @return The result of the invoked function. May be null.
     * @throws IllegalArgumentException If the provided identifier is not invocable.
     */
    fun invoke(scope: Scope): Any? {
        return when (identifier) {
            is Fn -> identifier.invoke(Scope(scope))
            is AnonymousFn -> identifier.invoke(args, Scope(scope))
            is String -> {
                when {
                    identifier.contains(".") && identifier.contains("/") -> {
                        val (className, methodName) = identifier.split("/")

                        val obj = Class.forName(className).kotlin.objectInstance!!
                        val functions = obj.javaClass.kotlin.memberFunctions
                        val method = functions.find { it.name == methodName }!!

                        val translated = args.map { if (it is String) scope.getReference(it) else it }

                        method.call(RT, *translated.toTypedArray(), scope)
                    }

                    identifier == "var" -> {
                        if (args.size != 2) {
                            error("Expected 2 arguments, got ${args.size}")
                        }

                        val name = args[0] as String
                        val value = args[1]

                        scope.setReference(name, value)

                        name
                    }

                    else -> {
                        val ref = scope.getReference(identifier)

                        if (ref is Fn) {
                            return ref.invoke(Scope(scope))
                        } else if (ref is AnonymousFn) {
                            return ref.invoke(
                                args.map { if (it is String) scope.getReference(it) else it },
                                Scope(scope)
                            )
                        }

                        null
                    }
                }
            }

            else -> error("Invalid function identifier type ${identifier!!::class.simpleName}")
        }
    }

    override fun toString(): String {
        return "Fn($identifier ${args.joinToString(" ")})"
    }
}

/**
 * Represents a map.
 *
 * @param elements The elements of the map.
 * @constructor Creates a map with the provided elements.
 */
data class Mp(val elements: Map<Any?, Any?>) {

    constructor(vararg elements: Pair<Any?, Any?>) : this(elements.toMap())

    operator fun get(key: Any?): Any? {
        return elements[key]
    }

    override fun toString(): String {
        val entries = elements.entries.joinToString(", ") { "${it.key}: ${it.value}" }

        return "{$entries}"
    }
}

data class Arr(val values: List<Any?>) {

    constructor(vararg values: Any?) : this(values.toList())

    val size get() = values.size

    fun drop(n: Int): Arr {
        return Arr(values.drop(n))
    }

    fun take(n: Int): Arr {
        return Arr(values.take(n))
    }

    operator fun get(index: Int): Any? {
        return values[index]
    }

    fun joinToString(separator: String): String {
        return values.joinToString(separator)
    }

    override fun toString(): String {
        return "[${values.joinToString(" ")}]"
    }

    operator fun iterator(): Iterator<Any?> {
        return values.iterator()
    }
}

private fun error(message: String) {
    throw IllegalArgumentException(message)
}