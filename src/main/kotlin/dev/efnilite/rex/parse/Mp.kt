package dev.efnilite.rex.parse

data class Mp(val elements: Map<Any?, Any?>) {

    override fun toString(): String {
        val entries = elements.entries.joinToString(", ") { "${it.key}: ${it.value}" }

        return "{$entries}"
    }
}