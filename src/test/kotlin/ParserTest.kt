import dev.efnilite.rex.parse.Parser
import dev.efnilite.rex.parse.Parser.parse
import dev.efnilite.rex.token.Tokenizer.Companion.tokenize
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

object ParserTest {

    @Test
    fun testParse() {
        println(parse(tokenize("(+ {2 [3 'false'] nil (- 2 2)} 1 2)")))
    }

    @Test
    fun testMp() {
        assertFails { parse(tokenize("{2 3 4}")) }
    }

    @Test
    fun testFn() {
        assertFails { parse(tokenize("(2 3 4)")) }
    }
}