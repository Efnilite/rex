package dev.efnilite.rex.token

/**
 * An int literal.
 * @property value The number value.
 */
data class IntLiteral(override val value: Int) : Token, Literal<Number>