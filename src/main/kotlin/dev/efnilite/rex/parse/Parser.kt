package dev.efnilite.rex.parse

import dev.efnilite.rex.token.*

object Parser {

    /**
     * Parse the provided tokens into a list of objects.
     * @param tokens The tokens to parse.
     * @return The parsed list of objects.
     */
    fun parse(tokens: List<Token>): List<Any?> {
        val list = mutableListOf<Any?>()

        for (token in tokens) {
            list.add(parse(token))
        }

        return list
    }

    private fun parse(token: Token): Any? {
        return when (token) {
            is FnToken -> parseFn(token)
            is MapToken -> parseMap(token)
            is ArrToken -> parseArr(token)
            is Literal<*> -> token.value
            else -> error(token, "Invalid token")
        }
    }

    private fun parseFn(fn: FnToken): Fn {
        val tokens = fn.tokens
        val identifier = tokens[0] as Identifier

        return Fn(identifier.value, tokens.drop(1).map { parse(it) }, Scope(null))
    }

    private fun parseMap(mp: MapToken): Mp {
        val map = mutableMapOf<Any?, Any?>()

        for (pair in mp.tokens.chunked(2)) {
            if (pair.size % 2 != 0) {
                error(mp, "Map must have an even number of elements")
            }

            map[parse(pair[0])] = parse(pair[1])
        }

        return Mp(map)
    }

    private fun parseArr(arr: ArrToken): Arr {
        val list = mutableListOf<Any?>()

        for (token in arr.tokens) {
            list.add(parse(token))
        }

        return Arr(list)
    }

    private fun error(token: Token, message: String) {
        throw IllegalArgumentException(
            "Error at line TODO, character TODO\n$token^ $message"
        )
    }
}