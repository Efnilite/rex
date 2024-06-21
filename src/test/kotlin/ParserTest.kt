import dev.efnilite.rex.Parser.parse
import dev.efnilite.rex.Tokenizer.Companion.tokenize
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

object ParserTest {

    @Test
    fun testParse() {
        println(parse(tokenize("(+ {2 [3 'false'] nil (- 2 2)} 1 2)")))
        println(parse(tokenize("(defn add [a b] (+ a b))")))
    }

    @Test
    fun testReferencing() {
        val result = parse(tokenize("(dev.efnilite.rex.RT/add 1 2)"))

        assertIs<Int>(result)
        assertEquals(3, result)
    }

    @Test
    fun testDef() {
        val result = parse(tokenize("(def + (fn [x y] (dev.efnilite.rex.RT/add x y))) (+ 1 2)"))

        println(result)
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