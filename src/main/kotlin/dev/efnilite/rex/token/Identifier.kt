package dev.efnilite.rex.token

/**
 * An identifier.
 * @property value The identifier value.
 */
data class Identifier(override val value: String) : Token, Literal<String>