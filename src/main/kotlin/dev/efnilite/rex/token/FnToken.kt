package dev.efnilite.rex.token

/**
 * Any list of tokens in an S-expression.
 * @property tokens The tokens in the function call.
 */
data class FnToken(val tokens: List<Token>) : Token