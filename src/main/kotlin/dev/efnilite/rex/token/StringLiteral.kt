package dev.efnilite.rex.token

/**
 * A string literal.
 * @property value The string value.
 */
data class StringLiteral(override val value: String) : Token, Literal<String>