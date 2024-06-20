package dev.efnilite.rex.token

import java.io.File
import kotlin.math.max

/**
 * Class for tokenizing the provided string.
 */
class Tokenizer(string: String) {

    private var chars = string.toCharArray()
    private var pos = 0

    private var line = 1
    private var posInLine = 1
    private var lineSoFar = ""

    fun tokenize(): ProgramToken {
        return tokenizeRecursive(null, "Reached end of file") { ProgramToken(it) } as ProgramToken
    }

    private fun tokenizeFn(): Token {
        return tokenizeRecursive(')', "Missing closing parenthesis") { FnToken(it) }
    }

    private fun tokenizeArr(): Token {
        return tokenizeRecursive(']', "Missing closing brackets") { ArrToken(it) }
    }

    private fun tokenizeMp(): Token {
        return tokenizeRecursive('}', "Missing closing curly brackets") { MapToken(it) }
    }

    // go to next character
    private fun next() {
        lineSoFar += chars[pos]
        pos++
        posInLine++

        if (pos < chars.size && chars[pos] == '\n') {
            line++
            posInLine = 1
            lineSoFar = ""
        }
    }

    private fun tokenizeRecursive(endChar: Char?, errorMessage: String, collector: (List<Token>) -> Token): Token {
        val tokens = mutableListOf<Token>()
        var token = ""

        var isString = false
        var isComment = false

        while (true) {
            if (pos == chars.size) {
                when {
                    isString -> error("Missing closing string quote")
                    endChar != null -> error(errorMessage)
                }

                break
            }

            val char = chars[pos]

            when {
                isString && char == '\\' -> {
                    token += chars[pos + 1]
                    next()
                    next()
                    continue
                }
                isString && char != '\'' -> {
                    token += char
                    next()
                    continue
                }
                isComment && char != '\n' -> {
                    next()
                    continue
                }
                char.isWhitespace() -> {
                    isComment = false

                    if (token.isNotEmpty()) {
                        tokens += parseToken(token)
                    }
                    token = ""
                    next()
                    continue
                }
            }

            when (char) {
                // for functions, arrays
                endChar -> {
                    next()
                    break
                }

                // skip commas not in strings
                ',' -> {
                    next()
                }

                '\'' -> {
                    if (isString) {
                        tokens += StringToken(token)
                        isString = false
                        token = ""
                    } else if (token.isEmpty()) {
                        isString = true
                    } else {
                        token += char
                    }
                    next()
                }

                '{' -> {
                    next()
                    tokens += tokenizeMp()
                }

                '[' -> {
                    next()
                    tokens += tokenizeArr()
                }

                '(' -> {
                    next()
                    tokens += tokenizeFn()
                }

                '#' -> {
                    isComment = true
                    next()
                }

                else -> {
                    checkLegality(char)

                    token += char
                    next()
                }
            }
        }

        if (token.isNotEmpty()) {
            tokens += parseToken(token)
        }

        return collector(tokens)
    }

    private fun checkLegality(char: Char) {
        when (char) {
            '\\', '/', ')', ']', '}', '#', '\'' -> {
                error("Illegal identifier")
            }
        }
    }

    private fun parseToken(token: String): Token {
        return when {
            token.matches(Regex("-?\\d*\\.?\\d+")) -> {
                if (!token.contains(".")) {
                    try {
                        IntToken(token.toInt())
                    } catch (e: NumberFormatException) {
                        LongToken(token.toLong())
                    }
                } else {
                    DoubleToken(token.toDouble())
                }
            }

            token.lowercase() == "true" -> BooleanToken(true)
            token.lowercase() == "false" -> BooleanToken(false)
            token.lowercase() == "nil" -> NilToken()
            else -> IdentifierToken(token)
        }
    }

    private fun error(message: String) {
        val fullLine = {
            var line = lineSoFar

            while (pos < chars.size && chars[pos] != '\n') {
                line += chars[pos]
                pos++
            }

            line
        }

        throw IllegalArgumentException(
            "Error at line $line, character $posInLine\n" +
            "${fullLine().replace("\n", "")}\n" +
            "${" ".repeat(max(0, posInLine - 1))}^ $message"
        )
    }

    companion object {
        /**
         * Tokenizes the provided string.
         * @param string the string to tokenize.
         * @return a list of tokens.
         * @throws IllegalArgumentException if the provided string contains syntactic errors.
         */
        fun tokenize(string: String): List<Token> {
            return Tokenizer(string).tokenize().tokens
        }

        /**
         * Tokenizes the provided file.
         * @param file the file to tokenize.
         * @return a list of tokens.
         * @throws IllegalArgumentException if the provided file contains syntactic errors.
         */
        fun tokenize(file: File): List<Token> {
            return Tokenizer(file.readLines().joinToString("\n")).tokenize().tokens
        }
    }
}