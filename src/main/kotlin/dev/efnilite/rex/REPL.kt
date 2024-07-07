package dev.efnilite.rex

/**
 * @author <a href='https://efnilite.dev'>Efnilite</a>
 */
fun main() {
    startRepl()
}

private fun startRepl() {
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
    return object {}.javaClass.classLoader.getResourceAsStream("core.rx")!!.bufferedReader().use { it.readText() }
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