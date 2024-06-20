package dev.efnilite.rex.parse

data class Arr(val values: List<Any?>) {

    override fun toString(): String {
        return "[${values.joinToString(" ")}]"
    }
}