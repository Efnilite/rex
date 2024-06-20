package dev.efnilite.rex.token

/**
 * An int literal.
 * @property value The number value.
 */
data class IntToken(override val value: Int) : Token, Literal<Number>