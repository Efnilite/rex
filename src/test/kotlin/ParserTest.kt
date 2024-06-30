import dev.efnilite.rex.Arr
import dev.efnilite.rex.DefinedFn
import dev.efnilite.rex.Mp
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
        println(parse(tokenize("(def add [a b] (+ a b))")))
    }

    @Test
    fun testReferencing() {
        assertEquals(3, parse(tokenize("(dev.efnilite.rex.RT/add 1 2)")))
    }

    @Test
    fun testDefinedFnAsIdentifier() {
        assertEquals(3, parse(tokenize("((fn [x y] (dev.efnilite.rex.RT/add x y)) 1 2)")))

        parse(tokenize("(var x 5) ((fn [x] (dev.efnilite.rex.RT/add x x)) 2)")).let {
            it as List<*>

            assertEquals("x", it[0])
            assertEquals(4, it[1])
        }

        parse(tokenize("(var x 2) ((fn [x'] (dev.efnilite.rex.RT/add x x')) 1)")).let {
            it as List<*>

            assertEquals("x", it[0])
            assertEquals(3, it[1])
        }
    }

    @Test
    fun testVar() {
        parse(tokenize("(var + (fn [x y] (dev.efnilite.rex.RT/add x y))) (+ 1 2)")).let {
            it as List<*>

            assertEquals("+", it[0])
            assertEquals(3, it[1])
        }

        parse(tokenize("(var + (fn [x y] (dev.efnilite.rex.RT/add x y))) ((fn [x y] (+ x y)) 1 2)")).let {
            it as List<*>

            assertEquals("+", it[0])
            assertEquals(3, it[1])
        }

        assertFails { parse(tokenize("(var var)")) }
        assertFails { parse(tokenize("(var true false)")) }
        assertFails { parse(tokenize("(var 1 2)")) }
        assertFails { parse(tokenize("(var nil 0)")) }
        assertFails { parse(tokenize("(var [{a 2 b 4} x] 0)")) }
        assertFails { parse(tokenize("(var {} 0)")) }
        assertFails { parse(tokenize("(var (fn [x] x) 0)")) }
    }

    @Test
    fun testArray() {
        val result = parse(tokenize("[2 3 4]"))

        result as Arr

        assertEquals(2, result[0])
        assertEquals(3, result[1])
        assertEquals(4, result[2])
    }

    @Test
    fun testMap() {
        assertFails { parse(tokenize("{2 3 4}")) }

        parse(tokenize("{2 3 'balls' (fn [x] x)}")).let {
            it as Mp

            assertEquals(3, it[2])
            assertIs<DefinedFn>(it["balls"])
        }
    }

    @Test
    fun testFn() {
        assertFails { parse(tokenize("(2 3 4)")) }

        parse(tokenize("(var x 3) (var + (fn [x y] (dev.efnilite.rex.RT/add x y))) (+ 2 1)")).let {
            it as List<*>

            assertEquals("x", it[0])
            assertEquals("+", it[1])
            assertEquals(3, it[2])
        }
    }

    @Test
    fun testFnCallScope() {
        assertEquals(10, parse(tokenize("(dev.efnilite.rex.RT/reduce (fn [acc x] (dev.efnilite.rex.RT/add acc x)) 0 [1 2 3 4])")))
    }
}