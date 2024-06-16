package dev.efnilite.rex.token

/**
 * A literal value, such as a string or number.
 */
interface Literal<T> {
    val value: T
}