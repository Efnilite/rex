package dev.efnilite.rex.token

/**
 * A double literal.
 * @property value The number value.
 */
data class DoubleLiteral(override val value: Double) : Token, Literal<Number>