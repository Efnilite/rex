package dev.efnilite.rex.token

/**
 * A boolean literal.
 * @property value The boolean value.
 */
data class BooleanToken(override val value: Boolean) : Token, Literal<Boolean>