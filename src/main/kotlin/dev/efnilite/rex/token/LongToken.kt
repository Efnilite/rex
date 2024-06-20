package dev.efnilite.rex.token

/**
 * A long literal.
 * @property value The number value.
 */
data class LongToken(override val value: Long) : Token, Literal<Number>