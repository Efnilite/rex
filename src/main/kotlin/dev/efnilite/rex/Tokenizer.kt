package dev.efnilite.rex

import java.io.File
import kotlin.math.max

/**
 * Class for tokenizing the provided string.
 *
 * @author <a href='https://efnilite.dev'>Efnilite</a>
 */
class Tokenizer(string: String) {

    private var chars = string.toCharArray()
    private var pos = 0

    private var line = 1
    private var posInLine = 1
    private var lineSoFar = ""

    fun getTokens(): List<Token> {
        return (tokenizeRecursive(null, "Reached end of file") { ProgramToken(it) } as ProgramToken).tokens
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
                isString && char != '\"' -> {
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

                '\"' -> {
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
                    when (char) {
                        '\\', ')', ']', '}' -> {
                            error("Illegal identifier")
                        }
                    }

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

    private fun parseToken(token: String): Token {
        return when {
            token.matches(INT_OR_LONG_REGEX) -> {
                try {
                    IntToken(token.replace("_", "").toInt())
                } catch (e: NumberFormatException) {
                    LongToken(token.replace("_", "").toLong())
                }
            }
            token.matches(DOUBLE_REGEX) -> DoubleToken(token.replace("_", "").toDouble())
            token == "true" -> BooleanToken(true)
            token == "false" -> BooleanToken(false)
            token == "nil" -> NilToken()
            else -> IdentifierToken(token)
        }
    }

    private fun error(message: String): Nothing {
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

private val DOUBLE_REGEX = Regex("-?\\d*\\.(\\d|_)+")
private val INT_OR_LONG_REGEX = Regex("-?(\\d|_)+")

private data class ProgramToken(val tokens: List<Token>) : Token

/**
 * Superclass of every valid thing.
 */
interface Token

/**
 * A map.
 * @property tokens The tokens in the map.
 */
data class MapToken(val tokens: List<Token>) : Token

/**
 * A boolean literal.
 * @property value The boolean value.
 */
data class NilToken(override val value: Nothing? = null) : Token, Literal<Nothing?>

/**
 * An array.
 * @property tokens The tokens in the array.
 */
data class ArrToken(val tokens: List<Token>) : Token

/**
 * Any list of tokens in an S-expression.
 * @property tokens The tokens in the function call.
 */
data class FnToken(val tokens: List<Token>) : Token

/**
 * A literal value, such as a string or number.
 */
interface Literal<T> {
    val value: T
}

/**
 * A string literal.
 * @property value The string value.
 */
data class StringToken(override val value: String) : Token, Literal<String>

/**
 * A long literal.
 * @property value The number value.
 */
data class LongToken(override val value: Long) : Token, Literal<Number>

/**
 * A boolean literal.
 * @property value The boolean value.
 */
data class BooleanToken(override val value: Boolean) : Token, Literal<Boolean>

/**
 * A double literal.
 * @property value The number value.
 */
data class DoubleToken(override val value: Double) : Token, Literal<Number>

/**
 * An identifier.
 * @property value The identifier value.
 */
data class IdentifierToken(override val value: String) : Token, Literal<String>

/**
 * An int literal.
 * @property value The number value.
 */
data class IntToken(override val value: Int) : Token, Literal<Number>

/**
 * Tokenizes the provided string.
 * @param string the string to tokenize.
 * @return a list of tokens.
 * @throws IllegalArgumentException if the provided string contains syntactic errors.
 */
fun tokenize(string: String): List<Token> {
    return Tokenizer(string).getTokens()
}

/**
 * Tokenizes the provided file.
 * @param file the file to tokenize.
 * @return a list of tokens.
 * @throws IllegalArgumentException if the provided file contains syntactic errors.
 */
fun tokenize(file: File): List<Token> {
    return Tokenizer(file.readLines().joinToString("\n")).getTokens()
}