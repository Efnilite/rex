package dev.efnilite.rex

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

    fun tokenize(): Program {
        return tokenizeRecursive(null, "Reached end of file") { Program(it) } as Program
    }

    private fun tokenizeFn(): Token {
        return tokenizeRecursive(')', "Missing closing parenthesis") { FnToken(it) }
    }

    private fun tokenizeArr(): Token {
        return tokenizeRecursive(']', "Missing closing brackets") { Arr(it) }
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

                '\'' -> {
                    if (isString) {
                        tokens += StringLiteral(token)
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
                        IntLiteral(token.toInt())
                    } catch (e: NumberFormatException) {
                        LongLiteral(token.toLong())
                    }
                } else {
                    DoubleLiteral(token.toDouble())
                }
            }

            token.lowercase() == "true" -> BooleanLiteral(true)
            token.lowercase() == "false" -> BooleanLiteral(false)
            else -> Identifier(token)
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
}

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

/**
 * Superclass of every valid thing.
 */
interface Token

/**
 * A literal value, such as a string or number.
 */
interface Literal<T> {
    val value: T
}

/**
 * A double literal.
 * @property value The number value.
 */
data class DoubleLiteral(override val value: Double) : Token, Literal<Number>

/**
 * An int literal.
 * @property value The number value.
 */
data class IntLiteral(override val value: Int) : Token, Literal<Number>

/**
 * A long literal.
 * @property value The number value.
 */
data class LongLiteral(override val value: Long) : Token, Literal<Number>

/**
 * A boolean literal.
 * @property value The boolean value.
 */
data class BooleanLiteral(override val value: Boolean) : Token, Literal<Boolean>

/**
 * A string literal.
 * @property value The string value.
 */
data class StringLiteral(override val value: String) : Token, Literal<String>

/**
 * An identifier.
 * @property value The identifier value.
 */
data class Identifier(override val value: String) : Token, Literal<String>

/**
 * Holds all tokens in the program.
 */
data class Program(val tokens: List<Token>) : Token

/**
 * An array.
 * @property tokens The tokens in the array.
 */
data class Arr(val tokens: List<Token>) : Token

/**
 * Any list of tokens in an S-expression.
 * @property tokens The tokens in the function call.
 */
data class FnToken(val tokens: List<Token>) : Token

/**
 * A map.
 * @property tokens The tokens in the map.
 */
data class MapToken(val tokens: List<Token>) : Token