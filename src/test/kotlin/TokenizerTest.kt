import dev.efnilite.rex.token.*
import dev.efnilite.rex.token.Tokenizer.Companion.tokenize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFails

object TokenizerTest {

    private fun assertLiteral(expected: Any?, type: KClass<out Literal<*>>, singular: Any) {
        assert(singular::class == type) { "Expected ${type.simpleName}, got ${singular::class.simpleName}" }
        assertEquals(expected, (singular as Literal<*>).value)
    }

    @Test
    @Timeout(1)
    fun testEmpty() {
        val tokens = tokenize("")

        assertEquals(0, tokens.size)
    }

    @Test
    @Timeout(1)
    fun testWhitespace() {
        tokenize(" \n \t \r        true").let {
            assertEquals(1, it.size)
            assertLiteral(true, BooleanToken::class, it[0])
        }

        tokenize("[      \t\r true        \n2   ]").let {
            assertEquals(1, it.size)

            assert(it[0] is ArrToken)
            val arrToken = it[0] as ArrToken
            assertEquals(2, arrToken.tokens.size)

            assertLiteral(true, BooleanToken::class, arrToken.tokens[0])
            assertLiteral(2, IntToken::class, arrToken.tokens[1])
        }
    }

    @Test
    @Timeout(1)
    fun testComment() {
        assertEquals(0, tokenize("#").size)
        assertFails { tokenize("(ident # this comment removes the rest of the line)") }

        tokenize("# this is a comment\ntrue#this is another comment#thisaswell").let {
            assertEquals(1, it.size)
            assertLiteral(true, BooleanToken::class, it[0])
        }
    }

    @Test
    @Timeout(1)
    fun testBoolean() {
        val tokens = tokenize("true false")

        assertEquals(2, tokens.size)
        assertLiteral(true, BooleanToken::class, tokens[0])
        assertLiteral(false, BooleanToken::class, tokens[1])
    }

    @Test
    @Timeout(1)
    fun testNumber() {
        val tokens = tokenize("0 0.0 .0 -0.0 -.0 -.2235235 34589345.013593 435807345009 -3495834598345347234")

        assertEquals(9, tokens.size)
        assertLiteral(0, IntToken::class, tokens[0])
        assertLiteral(0.0, DoubleToken::class, tokens[1])
        assertLiteral(0.0, DoubleToken::class, tokens[2])
        assertLiteral(-0.0, DoubleToken::class, tokens[3])
        assertLiteral(-0.0, DoubleToken::class, tokens[4])
        assertLiteral(-0.2235235, DoubleToken::class, tokens[5])
        assertLiteral(34589345.013593, DoubleToken::class, tokens[6])
        assertLiteral(435807345009, LongToken::class, tokens[7])
        assertLiteral(-3495834598345347234, LongToken::class, tokens[8])
    }

    @Test
    @Timeout(1)
    fun testString() {
        assertFails { tokenize("'hello") }

        tokenize("'\"hey there, how\\'s it going?\", he said. \\''").let {
            assertEquals(1, it.size)
            assertLiteral("\"hey there, how's it going?\", he said. '", StringToken::class, it[0])
        }

        tokenize("['hello' {'xx' 'there'}]").let {
            assertEquals(1, it.size)
            assert(it[0] is ArrToken)
            val arrToken = it[0] as ArrToken
            assertEquals(2, arrToken.tokens.size)
            assertLiteral("hello", StringToken::class, arrToken.tokens[0])

            run {
                assert(arrToken.tokens[1] is MapToken)
                val mapToken = arrToken.tokens[1] as MapToken
                assertEquals(2, mapToken.tokens.size)
                assertLiteral("xx", StringToken::class, mapToken.tokens[0])
                assertLiteral("there", StringToken::class, mapToken.tokens[1])
            }
        }
    }

    @Test
    @Timeout(1)
    fun testIdentifier() {
        assertFails { tokenize("hello{there") }
        assertFails { tokenize("hello]]there") }

        tokenize("really-long-method_name+that->is-very_annoying? something^else!").let {
            assertEquals(2, it.size)
            assertLiteral("really-long-method_name+that->is-very_annoying?", IdentifierToken::class, it[0])
            assertLiteral("something^else!", IdentifierToken::class, it[1])
        }

        tokenize("deri'vative'").let {
            assertEquals(1, it.size)
            assertLiteral("deri'vative'", IdentifierToken::class, it[0])
        }

        tokenize("e2.3").let {
            assertEquals(1, it.size)
            assertLiteral("e2.3", IdentifierToken::class, it[0])
        }
    }

    @Test
    @Timeout(1)
    fun testNil() {
        val tokens = tokenize("nil")

        assertEquals(1, tokens.size)
        assertLiteral(null, NilToken::class, tokens[0])
    }


    @Test
    @Timeout(1)
    fun testArr() {
        val tokens = tokenize("3 [-.21 'hey there' [true [(fn [x] x) '[rar]']] false]")

        assertEquals(2, tokens.size)
        assertLiteral(3, IntToken::class, tokens[0])

        assert(tokens[1] is ArrToken)
        val arrToken1 = tokens[1] as ArrToken
        assertEquals(4, arrToken1.tokens.size)
        assertLiteral(-0.21, DoubleToken::class, arrToken1.tokens[0])
        assertLiteral("hey there", StringToken::class, arrToken1.tokens[1])

        run {
            assert(arrToken1.tokens[2] is ArrToken)
            val arrToken2 = arrToken1.tokens[2] as ArrToken
            assertEquals(2, arrToken2.tokens.size)
            assertLiteral(true, BooleanToken::class, arrToken2.tokens[0])

            run {
                assert(arrToken2.tokens[1] is ArrToken)
                val arrToken3 = arrToken2.tokens[1] as ArrToken
                assertEquals(2, arrToken3.tokens.size)

                run {
                    assert(arrToken3.tokens[0] is FnToken)
                    val fnToken = arrToken3.tokens[0] as FnToken
                    assertEquals(3, fnToken.tokens.size)
                    assertLiteral("fn", IdentifierToken::class, fnToken.tokens[0])

                    assert(fnToken.tokens[1] is ArrToken)
                    val arrToken4 = fnToken.tokens[1] as ArrToken
                    assertEquals(1, arrToken4.tokens.size)
                    assertLiteral("x", IdentifierToken::class, arrToken4.tokens[0])

                    assertLiteral("x", IdentifierToken::class, fnToken.tokens[2])
                }

                assertLiteral("[rar]", StringToken::class, arrToken3.tokens[1])
            }
        }

        assertLiteral(false, BooleanToken::class, arrToken1.tokens[3])
    }

    @Test
    @Timeout(1)
    fun testNonNestedFunction() {
        val tokens = tokenize("'sup' .23 (map some 1 -2.01 true 'hello there!' ['a' 1]) false")

        assertEquals(4, tokens.size)
        assertLiteral("sup", StringToken::class, tokens[0])
        assertLiteral(0.23, DoubleToken::class, tokens[1])

        run {
            assert(tokens[2] is FnToken)
            val map = tokens[2] as FnToken
            assertEquals(7, map.tokens.size)
            assertLiteral("map", IdentifierToken::class, map.tokens[0])
            assertLiteral("some", IdentifierToken::class, map.tokens[1])
            assertLiteral(1, IntToken::class, map.tokens[2])
            assertLiteral(-2.01, DoubleToken::class, map.tokens[3])
            assertLiteral(true, BooleanToken::class, map.tokens[4])
            assertLiteral("hello there!", StringToken::class, map.tokens[5])

            run {
                val arrToken = map.tokens[6] as ArrToken
                assertEquals(2, arrToken.tokens.size)
                assertLiteral("a", StringToken::class, arrToken.tokens[0])
                assertLiteral(1, IntToken::class, arrToken.tokens[1])
            }
        }

        assertLiteral(false, BooleanToken::class, tokens[3])
    }

    @Test
    @Timeout(1)
    fun testNestedFunctions() {
        val tokens = tokenize("(map (fn [x] (let [y 2] (+ x y))) [2 2])")

        assert(tokens[0] is FnToken)
        val map = tokens[0] as FnToken
        assertEquals(3, map.tokens.size)
        assertLiteral("map", IdentifierToken::class, map.tokens[0])

        run {
            assert(map.tokens[1] is FnToken)
            val fnToken = map.tokens[1] as FnToken
            assertEquals(3, fnToken.tokens.size)
            assertLiteral("fn", IdentifierToken::class, fnToken.tokens[0])

            run {
                assert(fnToken.tokens[1] is ArrToken)
                val arrToken = fnToken.tokens[1] as ArrToken
                assertLiteral("x", IdentifierToken::class, arrToken.tokens[0])
            }

            run {
                assert(fnToken.tokens[2] is FnToken)
                val let = fnToken.tokens[2] as FnToken
                assertEquals(3, let.tokens.size)
                assertLiteral("let", IdentifierToken::class, let.tokens[0])

                run {
                    assert(let.tokens[1] is ArrToken)
                    val arrToken2 = let.tokens[1] as ArrToken
                    assertEquals(2, arrToken2.tokens.size)
                    assertLiteral("y", IdentifierToken::class, arrToken2.tokens[0])
                    assertLiteral(2, IntToken::class, arrToken2.tokens[1])
                }

                run {
                    assert(let.tokens[2] is FnToken)
                    val plus = let.tokens[2] as FnToken
                    assertEquals(3, plus.tokens.size)
                    assertLiteral("+", IdentifierToken::class, plus.tokens[0])
                    assertLiteral("x", IdentifierToken::class, plus.tokens[1])
                    assertLiteral("y", IdentifierToken::class, plus.tokens[2])
                }
            }
        }

        assert(map.tokens[2] is ArrToken)
        val arrToken3 = map.tokens[2] as ArrToken
        assertEquals(2, arrToken3.tokens.size)
        assertLiteral(2, IntToken::class, arrToken3.tokens[0])
        assertLiteral(2, IntToken::class, arrToken3.tokens[1])
    }

    @Test
    @Timeout(1)
    fun testMap() {
        val tokens = tokenize("2.3 {is-cheese? '{no way}' {test [1 2 (fn [x] x)]} false}")

        assertEquals(2, tokens.size)
        assertLiteral(2.3, DoubleToken::class, tokens[0])

        assert(tokens[1] is MapToken)
        val mapToken = tokens[1] as MapToken
        assertEquals(4, mapToken.tokens.size)
        assertLiteral("is-cheese?", IdentifierToken::class, mapToken.tokens[0])
        assertLiteral("{no way}", StringToken::class, mapToken.tokens[1])

        run {
            assert(mapToken.tokens[2] is MapToken)
            val mapToken2 = mapToken.tokens[2] as MapToken
            assertEquals(2, mapToken2.tokens.size)
            assertLiteral("test", IdentifierToken::class, mapToken2.tokens[0])

            run {
                assert(mapToken2.tokens[1] is ArrToken)
                val arrToken = mapToken2.tokens[1] as ArrToken
                assertEquals(3, arrToken.tokens.size)
                assertLiteral(1, IntToken::class, arrToken.tokens[0])
                assertLiteral(2, IntToken::class, arrToken.tokens[1])

                run {
                    assert(arrToken.tokens[2] is FnToken)
                    val fnToken = arrToken.tokens[2] as FnToken
                    assertEquals(3, fnToken.tokens.size)
                    assertLiteral("fn", IdentifierToken::class, fnToken.tokens[0])

                    run {
                        assert(fnToken.tokens[1] is ArrToken)
                        val arrToken2 = fnToken.tokens[1] as ArrToken
                        assertEquals(1, arrToken2.tokens.size)
                        assertLiteral("x", IdentifierToken::class, arrToken2.tokens[0])
                    }

                    assertLiteral("x", IdentifierToken::class, fnToken.tokens[2])
                }
            }
        }

        assertLiteral(false, BooleanToken::class, mapToken.tokens[3])
    }

    @Test
    @Timeout(1)
    fun testMultipleLines() {
        val tokens = tokenize("true\nfalse\n(+\n1\n1)")

        assertEquals(3, tokens.size)
        assertLiteral(true, BooleanToken::class, tokens[0])
        assertLiteral(false, BooleanToken::class, tokens[1])

        assert(tokens[2] is FnToken)
        val plus = tokens[2] as FnToken
        assertEquals(3, plus.tokens.size)
        assertLiteral("+", IdentifierToken::class, plus.tokens[0])
        assertLiteral(1, IntToken::class, plus.tokens[1])
        assertLiteral(1, IntToken::class, plus.tokens[2])
    }

    @Test
    @Timeout(1)
    fun testFail() {
        assertFails { tokenize(")") }
        assertFails { tokenize("]") }
        assertFails { tokenize("}") }
        assertFails { tokenize("/") }
        assertFails { tokenize("\\") }
        assertFails { tokenize("'") }

        assertFails { tokenize("'(]") }
        assertFails { tokenize("{[{(fn [x] x} true ]}") }
        assertFails { tokenize("{ } true }") }
    }

    @Test
    @Timeout(1)
    fun testCommas() {
        val tokens = tokenize("1, 2.0, , 'hey, partner' 4")

        assertEquals(4, tokens.size)
        assertLiteral(1, IntToken::class, tokens[0])
        assertLiteral(2.0, DoubleToken::class, tokens[1])
        assertLiteral("hey, partner", StringToken::class, tokens[2])
        assertLiteral(4, IntToken::class, tokens[3])
    }
}