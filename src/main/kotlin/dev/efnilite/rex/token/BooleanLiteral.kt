package dev.efnilite.rex.token

/**
 * A boolean literal.
 * @property value The boolean value.
 */
data class BooleanLiteral(override val value: Boolean) : Token, Literal<Boolean>