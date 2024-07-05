import dev.efnilite.rex.AFn
import dev.efnilite.rex.Arr
import dev.efnilite.rex.Identifier
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
        parse(tokenize("(+ {2 [3 \"false\"] nil (- 2 2)} 1 2)"))
        parse(tokenize("(defn add [a b] (+ a b))"))
    }

    @Test
    fun testReferencing() {
        assertEquals(3, parse(tokenize("(dev.efnilite.rex.RT/add 1 2)")))
        assertEquals(3, parse(tokenize("(dev.efnilite.rex.RT/add 1 " +
                "((fn [x y] (dev.efnilite.rex.RT/add x y)) 1 1))")))
        assertEquals(3, parse(tokenize("(dev.efnilite.rex.RT/add 1 (dev.efnilite.rex.RT/add 1 1))")))
    }

    @Test
    fun testDefinedFnAsIdentifier() {
        assertEquals(5, parse(tokenize("((fn [x] x) 5)")))
        assertEquals(5, parse(tokenize("((fn [x] x) (dev.efnilite.rex.RT/add 2 3))")))

        assertEquals(Arr(5), parse(tokenize("((fn [x] [x]) 5)")))
        assertEquals(Arr(5, Arr(5)), parse(tokenize("((fn [x] [x [x]]) 5)")))

        assertEquals(Mp(0 to 1), parse(tokenize("((fn [x] {0 x}) 1)")))
        assertEquals(Mp(0 to Mp(0 to 1)), parse(tokenize("((fn [x] {0 {0 x}}) 1)")))

        assertEquals(3, parse(tokenize("((fn [x] 3) 5)")))
        assertEquals(3, parse(tokenize("((fn [x y] 3) 5 6)")))
        assertEquals(3, parse(tokenize("((fn [x y] (dev.efnilite.rex.RT/add x y)) 1 2)")))

        parse(tokenize("(var x 5) ((fn [x] (dev.efnilite.rex.RT/add x x)) 2)")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(4, it[1])
        }

        parse(tokenize("(var x 2) ((fn [x'] (dev.efnilite.rex.RT/add x x')) 1)")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(3, it[1])
        }
    }

    @Test
    fun testVar() {
        parse(tokenize("(var x 3) x")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(3, it[1])
        }

        parse(tokenize("(var x (dev.efnilite.rex.RT/add 1 2)) x")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(3, it[1])
        }

        parse(tokenize("(var + (fn [x y] (dev.efnilite.rex.RT/add x y))) (+ 1 2)")).let {
            it as List<*>

            assertEquals(Identifier("+"), it[0])
            assertEquals(3, it[1])
        }

        parse(tokenize("(var + (fn [x y] (dev.efnilite.rex.RT/add x y))) ((fn [x y] (+ x y)) 1 2)")).let {
            it as List<*>

            assertEquals(Identifier("+"), it[0])
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
        parse(tokenize("[2 3 4]")).let {
            it as Arr

            assertEquals(2, it[0])
            assertEquals(3, it[1])
            assertEquals(4, it[2])
        }

        parse(tokenize("(var x 1) [x 0]")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(Arr(1, 0), it[1])
        }

        parse(tokenize("(var x 1) [x [x]]")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(Arr(1, Arr(1)), it[1])
        }

        parse(tokenize("(var x 1) [x [(dev.efnilite.rex.RT/add x 2)]]")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(Arr(1, Arr(3)), it[1])
        }
    }

    @Test
    fun testMap() {
        assertFails { parse(tokenize("{2 3 4}")) }

        parse(tokenize("{2 3 \"balls\" (fn [x] x)}")).let {
            it as Mp

            assertEquals(3, it[2])
            assertIs<AFn>(it["balls"])
        }

        parse(tokenize("(var x 1) {\"x\" 0 0 x}")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(Mp("x" to 0, 0 to 1), it[1])
        }

        parse(tokenize("(var x 1) {\"x\" 0 0 {0 x}}")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(Mp("x" to 0, 0 to Mp(0 to 1)), it[1])
        }

        parse(tokenize("(var x 1) {0 {0 (dev.efnilite.rex.RT/add x 2)}}")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(Mp(0 to Mp(0 to 3)), it[1])
        }
    }

    @Test
    fun testFn() {
        assertFails { parse(tokenize("((fn [1.0] nil) nil)")) }

        assertEquals(1, parse(tokenize("((fn [[x y] z & more] x) [1 2] 3 4)")))

        parse(tokenize("(var x 1) (var y (fn [] x)) (y)")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(Identifier("y"), it[1])
            assertEquals(1, it[2])
        }

        assertFails { parse(tokenize("(2 3 4)")) }

        parse(tokenize("(var x 3) (var + (fn [x y] (dev.efnilite.rex.RT/add x y))) (+ 2 1)")).let {
            it as List<*>

            assertEquals(Identifier("x"), it[0])
            assertEquals(Identifier("+"), it[1])
            assertEquals(3, it[2])
        }

        assertEquals(10, parse(tokenize("(dev.efnilite.rex.RT/reduce " +
                "(fn [acc x] (dev.efnilite.rex.RT/add acc x)) 0 [1 2 3 4])")))

        parse(tokenize("(var + (fn [x y] (dev.efnilite.rex.RT/add x y))) " +
                "(dev.efnilite.rex.RT/reduce + 0 [1 2 3 4])")).let {
            it as List<*>

            assertEquals(Identifier("+"), it[0])
            assertEquals(10, it[1])
        }
    }

    @Test
    fun testVarargs() {
        assertFails { parse(tokenize("((fn [& more] more))")) }

        parse(tokenize("((fn [& more] more) 1 2 3 4 5)")).let {
            it as Arr

            assertEquals(Arr(1, 2, 3, 4, 5), it)
        }

        parse(tokenize("((fn [x y & more] [x y more]) 1 2 3 4 5)")).let {
            it as Arr

            assertEquals(1, it[0])
            assertEquals(2, it[1])
            assertEquals(Arr(3, 4, 5), it[2])
        }
    }

    @Test
    fun testDefn() {
        parse(tokenize("(defn + \"adds things.\" [] 0) (+)")).let {
            it as List<*>

            assertEquals(Identifier("+"), it[0])
            assertEquals(0, it[1])
        }

        parse(tokenize("(defn + \"adds things.\" [x] x) (+ 1)")).let {
            it as List<*>

            assertEquals(Identifier("+"), it[0])
            assertEquals(1, it[1])
        }

        parse(tokenize("(defn + \"adds things.\" [x y] (dev.efnilite.rex.RT/add x y)) (+ 1 2)")).let {
            it as List<*>

            assertEquals(Identifier("+"), it[0])
            assertEquals(3, it[1])
        }

        parse(tokenize("(defn + \"adds things.\" [] 0 [x] x [x y] (dev.efnilite.rex.RT/add x y) " +
                "[x y & more] (dev.efnilite.rex.RT/reduce + (+ x y) more)) (+) (+ 1) (+ 2 1) (+ 2 1 4) (+ 2 1 4 5 6)")).let {
            it as List<*>

            assertEquals(Identifier("+"), it[0])
            assertEquals(0, it[1])
            assertEquals(1, it[2])
            assertEquals(3, it[3])
            assertEquals(7, it[4])
            assertEquals(18, it[5])
        }

        parse(tokenize("(defn + [& more] " +
                "(dev.efnilite.rex.RT/reduce (fn [x y] (dev.efnilite.rex.RT/add x y)) more)) " +
                "(+ 1) (+ 1 2)")).let {
            it as List<*>

            assertEquals(Identifier("+"), it[0])
            assertEquals(1, it[1])
            assertEquals(3, it[2])
        }
    }
}