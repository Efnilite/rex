package dev.efnilite.rex.parse

import dev.efnilite.rex.token.*

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