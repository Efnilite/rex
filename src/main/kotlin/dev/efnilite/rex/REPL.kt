package dev.efnilite.rex

import dev.efnilite.rex.Parser.parse
import dev.efnilite.rex.Tokenizer.Companion.tokenize

object REPL {

    @JvmStatic
    fun main(args: Array<String>) {
        main()
    }

    private fun main() {
        val scope = Scope(null)

        eval(readCore(), scope)

        while (true) {
            print("rex> ")
            val input = readlnOrNull()?.trim() ?: break

            if (input.isNotEmpty()) {
                eval(input, scope)
            }
        }
    }

    private fun readCore(): String {
        return this::class.java.getResourceAsStream("/core.rx")!!.bufferedReader().use { it.readText() }
    }

    private fun eval(input: String, scope: Scope) {
        try {
            val tokens = tokenize(input)
            val result = parse(tokens, scope)

            println(result)
        } catch (e: Exception) {
            e.printStackTrace()
            println()
        }
    }
}