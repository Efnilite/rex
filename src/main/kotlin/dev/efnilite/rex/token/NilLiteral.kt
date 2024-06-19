package dev.efnilite.rex.token

/**
 * A boolean literal.
 * @property value The boolean value.
 */
data class NilLiteral(override val value: Nothing? = null) : Token, Literal<Nothing?>