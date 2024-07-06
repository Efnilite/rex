package dev.efnilite.rex

import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

/**
 * @author <a href='https://efnilite.dev'>Efnilite</a>
 */
object Parser {

    /**
     * Parse the provided tokens into a list of objects.
     *
     * @param tokens The tokens to parse.
     * @return The parsed list of objects.
     */
    fun parse(tokens: List<Token>, scope: Scope = Scope(null)): Any? {
        val parsed = tokens.map { invokeAny(parse(it), scope) }

        // avoids returning ["hey"] instead of "hey" when single element is parsed
        return if (parsed.size == 1) parsed[0] else parsed
    }

    private fun parse(token: Token): Any? {
        return when (token) {
            is FnToken -> parseFn(token)
            is MapToken -> parseMap(token)
            is ArrToken -> parseArr(token)
            is IdentifierToken -> Identifier(token.value)
            is Literal<*> -> token.value
            else -> error("Invalid token")
        }
    }

    private fun parseFn(fn: FnToken): Any {
        val tokens = fn.tokens
        val identifier = tokens[0]

        if (identifier is IdentifierToken) {
            when (identifier.value) {
                "fn" -> return AFn(parse(tokens[1]) as Arr, tokens.drop(2).map { parse(it) })
                "let" -> return BindingFn(parse(tokens[1]) as Arr, tokens.drop(2).map { parse(it) })
            }
        }

        return Fn(parse(identifier), tokens.drop(1).map { parse(it) })
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
        return Arr(arr.tokens.map { parse(it) })
    }
}

class Scope(private val parent: Scope? = null) {

    private val refs = mutableMapOf<String, Any?>()

    /**
     * Sets a reference in the scope.
     *
     * @param ref The reference.
     * @param value The value of the reference.
     */
    fun setReference(ref: Identifier, value: Any?) {
        refs[ref.value] = value
    }

    /**
     * Resolves a reference in the scope.
     * If the reference is not found in the current scope, it will look in the parent scopes recursively.
     *
     * @param ref The reference.
     * @return The value of the reference, or null if not found.
     */
    fun getReference(ref: Identifier): Any? {
        if (refs.containsKey(ref.value)) {
            return refs[ref.value]
        }

        return parent?.getReference(ref)
    }

    /**
     * Returns the root scope of the current scope.
     *
     * @return The root scope.
     */
    fun getRootScope(): Scope {
        var scope = this

        while (scope.parent != null) {
            scope = scope.parent!!
        }

        return scope
    }

    override fun toString(): String {
        var scope = this
        val scopes = mutableListOf<Scope>()
        scopes.add(scope)
        while (scope.parent != null) {
            scope.parent!!.let {
                scopes.add(it)
                scope = it
            }
        }
        scopes.reverse()

        var str = ""
        for ((indent, s) in scopes.withIndex()) {
            str += s.refs.entries.joinToString("\n") {
                "${"\t".repeat(indent)}${it.key}: ${it.value}"
            }
            str += "\n"
        }

        return str
    }
}

interface DeferredFunction {

    /**
     * Replaces the params with the provided arguments in the scope and evaluates the body.
     *
     * @param args The arguments to replace the params with.
     * @param scope The scope to evaluate the body in. Contains the parent scope.
     */
    fun invoke(args: List<Any?>, scope: Scope): Any?
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
class AFn(
    private val params: Arr,
    private val body: List<Any?>,
) : DeferredFunction {

    constructor(params: Arr, vararg body: Any?) : this(params, body.toList())

    private val varargs: Boolean

    init {
        if (params.contains(Identifier("&"))) {
            if (params.indexOf(Identifier("&")) + 1 != params.size - 1) error("& must be followed by one other argument")

            varargs = true
        } else {
            varargs = false
        }
    }

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
    override fun invoke(args: List<Any?>, scope: Scope): Any? {
        if (varargs) {
            if (params.size - 1 > args.size) {
                error("Expected at least ${params.size - 1} arguments, got ${args.size}")
            }
        } else {
            if (params.size != args.size) {
                error("Expected ${params.size} arguments, got ${args.size}")
            }
        }

        for ((idx, arg) in args.withIndex()) {
            if (varargs && params[idx] == Identifier("&")) {
                scope.setReference(params[idx + 1] as Identifier, Arr(args.drop(idx)))
                break
            }

            val result = if (arg is Fn) arg.invoke(scope) else arg

            when (val param = params[idx]) {
                is Identifier -> scope.setReference(param, result)
                is Arr -> destructArgsRecursively(param, result as Arr, scope)
                else -> error("Expected an identifier, got ${param?.javaClass?.simpleName} with value $param")
            }
        }

        var result: Any? = null
        for (any in body) {
            result = invokeAny(any, scope)
        }

        return result
    }

    private fun destructArgsRecursively(params: Arr, args: Arr, scope: Scope): Scope {
        for ((idx, param) in params.values.withIndex()) {
            when (param) {
                is Identifier -> scope.setReference(param, args[idx])
                is Arr -> destructArgsRecursively(param, args[idx] as Arr, scope)
                else -> error("Invalid argument")
            }
        }

        return scope
    }

    override fun toString(): String {
        return "(fn $params ${if (varargs) "(varargs) " else ""}${body.joinToString(" ")})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AFn) return false

        if (params != other.params) return false
        if (body != other.body) return false
        if (varargs != other.varargs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = params.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + varargs.hashCode()
        return result
    }
}

/**
 * Represents a function defined with `defn`.
 *
 * @param doc The documentation of the function.
 * @param fns The functions defined in the `defn`.
 * The key is the arity of the function.
 * `-arity` should be used for variadic functions.
 */
data class DefinedFn(val doc: String = "", val fns: Map<Int, AFn>) : DeferredFunction {

    override fun invoke(args: List<Any?>, scope: Scope): Any? {
        if (fns.containsKey(args.size)) {
            return fns[args.size]!!.invoke(args, scope)
        } else if (fns.any { it.key < 0 }) {
            val (paramCount, value) = fns.entries.firstOrNull { it.key < 0 }!!

            if (args.size < -paramCount) error("Expected at least ${-paramCount} arguments, got ${args.size}")

            return value.invoke(args, scope)
        } else {
            error("No function with arity ${args.size}")
        }
    }

    override fun toString(): String {
        return "(defn $doc $fns)"
    }
}

/**
 * Represents a function which is instantly invoked at evaluation time.
 */
interface SFunction {

    fun invoke(scope: Scope): Any?

}

private var currentEvaluatingScope = Scope(null)

fun getCurrentEvaluatingScope(): Scope {
    return currentEvaluatingScope
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
data class Fn(val identifier: Any?, val args: List<Any?>) : SFunction {

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
    override fun invoke(scope: Scope): Any? {
        return when (identifier) {
            is SFunction -> identifier.invoke(Scope(scope))
            is DeferredFunction -> identifier.invoke(args, Scope(scope))
            is Identifier -> {
                val name = identifier.value
                val result = when {
                    name.contains(".") && name.contains("/") -> invokeJVMStaticReference(scope)
                    name.contains(".") && !name.contains("/") -> invokeJVMInstanceReference(scope)
                    name == "var" -> invokeVar(scope)
                    name == "defn" -> invokeDefn(scope)
                    else -> invokeElse(scope)
                }

                // avoid getting chars
                return if (result is Char) result.toString() else result
            }

            else -> error("Invalid function identifier type ${identifier!!::class.simpleName}")
        }
    }

    // todo benchmark against using ::class.kotlin
    private fun invokeJVMInstanceReference(scope: Scope): Any? {
        val name = (identifier as Identifier).value.replaceFirst(".", "")
        val translated = args.map { invokeAny(it, scope) }
        val instance = translated[0]!!

        currentEvaluatingScope = scope

        val fn = instance.javaClass.methods.firstOrNull { it.name == name && it.parameters.size == args.size - 1 }

        return if (fn != null) {
            fn.invoke(instance, *translated.drop(1).toTypedArray())
        } else {
            val properties = instance.javaClass.fields.firstOrNull { it.name == name }

            if (properties != null) {
                properties.get(instance)
            } else {
                error("No method or property found with name $name as ${instance.javaClass.simpleName}")
            }
        }
    }

    private fun invokeJVMStaticReference(scope: Scope): Any? {
        val (className, methodName) = (identifier as Identifier).value.split("/")
        val translated = args.map { invokeAny(it, scope) }

        currentEvaluatingScope = scope

        val obj = Class.forName(className) ?: error("Class $className not found")
        // ignore `this` instance required for invocation
        val fn = obj.methods.firstOrNull { it.name == methodName && it.parameters.size == args.size }

        return if (fn != null) {
            return fn.invoke(RT, *translated.toTypedArray())
        } else {
            val properties = obj.fields.firstOrNull { it.name == methodName }

            if (properties != null) {
                properties.get(obj)
            } else {
                error("No method or property found with name $methodName as ${obj.simpleName}")
            }
        }
    }

    private fun invokeVar(scope: Scope): Identifier {
        if (args.size != 2) error("Expected 2 arguments, got ${args.size}")

        val ref = args[0] as Identifier

        scope.setReference(ref, invokeAny(args[1], scope))

        return ref
    }

    private fun invokeDefn(scope: Scope): Identifier {
        val ref = args[0] as Identifier
        val hasDoc = args[1] is String
        val pairs = args.drop(if (hasDoc) 2 else 1).chunked(2) // skip doc

        val fns = pairs.map { (params, body) ->
            if (params !is Arr) error("Invalid function definition")

            if (params.contains(Identifier("&"))) {
                return@map -(params.size - 1) to AFn(params, body)
            }
            return@map params.size to AFn(params, body)
        }.toMap()

        scope.getRootScope().setReference(ref, DefinedFn(if (hasDoc) args[1] as String else "", fns))

        return ref
    }

    private fun invokeElse(scope: Scope): Any? {
        return when (val ident = scope.getReference(identifier as Identifier)) {
            is SFunction -> ident.invoke(Scope(scope))
            is DeferredFunction -> ident.invoke(args.map { if (it is Identifier) scope.getReference(it) else it }, Scope(scope))
            else -> null
        }
    }

    override fun toString(): String {
        return "($identifier ${args.joinToString(" ")})"
    }
}

class BindingFn(
    private val vars: Arr,
    private val body: List<Any?>,
) : SFunction {

    constructor(vars: Arr, vararg body: Any?) : this(vars, body.toList())

    override fun invoke(scope: Scope): Any? {
        val lowerScope = Scope(scope)

        for ((key, value) in vars.values.chunked(2)) {
            val result = if (value is Fn) value.invoke(lowerScope) else value

            when (key) {
                is Identifier -> lowerScope.setReference(key, result)
                is Arr -> destructArgsRecursively(key, result as Arr, lowerScope)
                else -> error("Expected an identifier, got ${key?.javaClass?.simpleName} with value $key")
            }
        }

        var result: Any? = null
        for (any in body) {
            result = invokeAny(any, lowerScope)
        }

        return result
    }

    private fun destructArgsRecursively(params: Arr, args: Arr, scope: Scope): Scope {
        for ((idx, param) in params.values.withIndex()) {
            when (param) {
                is Identifier -> scope.setReference(param, args[idx])
                is Arr -> destructArgsRecursively(param, args[idx] as Arr, scope)
                else -> error("Invalid argument")
            }
        }

        return scope
    }

    override fun toString(): String {
        return "(let $vars ${body.joinToString(" ")})"
    }
}

/**
 * Represents an identifier.
 *
 * @param value The value of the identifier.
 */
data class Identifier(val value: String) {
    override fun toString() = value
}

/**
 * Represents a non-function collection that may contain an instance of itself.
 */
interface Recursable {

    fun resolve(scope: Scope): Recursable

}

/**
 * Represents a map.
 *
 * @param elements The elements of the map.
 * @constructor Creates a map with the provided elements.
 */
data class Mp(val elements: Map<Any?, Any?>) : Recursable {

    constructor(vararg elements: Pair<Any?, Any?>) : this(elements.toMap())

    val size get() = elements.size

    override fun resolve(scope: Scope): Mp {
        return Mp(elements.mapValues { invokeAny(it.value, scope)})
    }

    operator fun get(key: Any?): Any? {
        return elements[key]
    }

    override fun toString(): String {
        val entries = elements.entries.joinToString(", ") { "${it.key}: ${it.value}" }

        return "{$entries}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mp

        return elements == other.elements
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }
}

/**
 * Represents an array.
 *
 * @param values The values of the array.
 * @constructor Creates an array with the provided values.
 */
data class Arr(val values: List<Any?>) : Recursable {

    constructor(vararg values: Any?) : this(values.toList())

    val size get() = values.size

    override fun resolve(scope: Scope): Recursable {
        return Arr(values.map { invokeAny(it, scope) })
    }

    fun drop(n: Int): Arr {
        return Arr(values.drop(n))
    }

    fun take(n: Int): Arr {
        return Arr(values.take(n))
    }

    fun indexOf(value: Any?): Int {
        return values.indexOf(value)
    }

    fun contains(value: Any?): Boolean {
        return values.contains(value)
    }

    operator fun get(index: Int): Any? {
        return values[index]
    }

    operator fun iterator(): Iterator<Any?> {
        return values.iterator()
    }

    override fun toString(): String {
        return "[${values.joinToString(" ")}]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Arr

        return values == other.values
    }

    override fun hashCode(): Int {
        return values.hashCode()
    }
}

private fun error(message: String): Nothing {
    throw IllegalArgumentException(message)
}

private fun invokeAny(any: Any?, scope: Scope): Any? {
    return when (any) {
        is SFunction -> any.invoke(scope)
        is Identifier -> scope.getReference(any)
        is Recursable -> any.resolve(scope)
        else -> any
    }
}