package dev.efnilite.rex

import dev.efnilite.rex.Parser.parse
import dev.efnilite.rex.Tokenizer.Companion.tokenize

object REPL {

    private val scope = Scope(null)

    @JvmStatic
    fun main(args: Array<String>) {
        main()
    }

    private fun main() {
        while (true) {
            print("rex> ")
            val input = readlnOrNull() ?: break

            if (input.lowercase() == "(exit)") break

            try {
                val tokens = tokenize(input)
                val result = parse(tokens, scope)

                println(result)
            } catch (e: Exception) {
                System.err.println(e.message)
            }
        }
    }
}