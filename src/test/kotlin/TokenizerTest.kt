import dev.efnilite.rex.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFails

object TokenizerTest {

    private fun assertLiteral(expected: Any, type: KClass<out Literal<*>>, singular: Any) {
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
            assertLiteral(true, BooleanLiteral::class, it[0])
        }

        tokenize("[      \t\r true        \n2   ]").let {
            assertEquals(1, it.size)

            assert(it[0] is Arr)
            val arr = it[0] as Arr
            assertEquals(2, arr.tokens.size)

            assertLiteral(true, BooleanLiteral::class, arr.tokens[0])
            assertLiteral(2, IntLiteral::class, arr.tokens[1])
        }
    }

    @Test
    @Timeout(1)
    fun testComment() {
        assertEquals(0, tokenize("#").size)
        assertFails { tokenize("(ident # this comment removes the rest of the line)") }

        tokenize("# this is a comment\ntrue#this is another comment#thisaswell").let {
            assertEquals(1, it.size)
            assertLiteral(true, BooleanLiteral::class, it[0])
        }
    }

    @Test
    @Timeout(1)
    fun testBoolean() {
        val tokens = tokenize("true false")

        assertEquals(2, tokens.size)
        assertLiteral(true, BooleanLiteral::class, tokens[0])
        assertLiteral(false, BooleanLiteral::class, tokens[1])
    }

    @Test
    @Timeout(1)
    fun testNumber() {
        val tokens = tokenize("0 0.0 .0 -0.0 -.0 -.2235235 34589345.013593 435807345009 -3495834598345347234")

        assertEquals(9, tokens.size)
        assertLiteral(0, IntLiteral::class, tokens[0])
        assertLiteral(0.0, DoubleLiteral::class, tokens[1])
        assertLiteral(0.0, DoubleLiteral::class, tokens[2])
        assertLiteral(-0.0, DoubleLiteral::class, tokens[3])
        assertLiteral(-0.0, DoubleLiteral::class, tokens[4])
        assertLiteral(-0.2235235, DoubleLiteral::class, tokens[5])
        assertLiteral(34589345.013593, DoubleLiteral::class, tokens[6])
        assertLiteral(435807345009, LongLiteral::class, tokens[7])
        assertLiteral(-3495834598345347234, LongLiteral::class, tokens[8])
    }

    @Test
    @Timeout(1)
    fun testString() {
        assertFails { tokenize("'hello") }

        tokenize("'\"hey there, how\\'s it going?\", he said. \\''").let {
            assertEquals(1, it.size)
            assertLiteral("\"hey there, how's it going?\", he said. '", StringLiteral::class, it[0])
        }

        tokenize("['hello' {'xx' 'there'}]").let {
            assertEquals(1, it.size)
            assert(it[0] is Arr)
            val arr = it[0] as Arr
            assertEquals(2, arr.tokens.size)
            assertLiteral("hello", StringLiteral::class, arr.tokens[0])

            run {
                assert(arr.tokens[1] is MapToken)
                val mapToken = arr.tokens[1] as MapToken
                assertEquals(2, mapToken.tokens.size)
                assertLiteral("xx", StringLiteral::class, mapToken.tokens[0])
                assertLiteral("there", StringLiteral::class, mapToken.tokens[1])
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
            assertLiteral("really-long-method_name+that->is-very_annoying?", Identifier::class, it[0])
            assertLiteral("something^else!", Identifier::class, it[1])
        }

        tokenize("deri'vative'").let {
            assertEquals(1, it.size)
            assertLiteral("deri'vative'", Identifier::class, it[0])
        }

        tokenize("e2.3").let {
            assertEquals(1, it.size)
            assertLiteral("e2.3", Identifier::class, it[0])
        }
    }

    @Test
    @Timeout(1)
    fun testArr() {
        val tokens = tokenize("3 [-.21 'hey there' [true [(fn [x] x) '[rar]']] false]")

        assertEquals(2, tokens.size)
        assertLiteral(3, IntLiteral::class, tokens[0])

        assert(tokens[1] is Arr)
        val arr1 = tokens[1] as Arr
        assertEquals(4, arr1.tokens.size)
        assertLiteral(-0.21, DoubleLiteral::class, arr1.tokens[0])
        assertLiteral("hey there", StringLiteral::class, arr1.tokens[1])

        run {
            assert(arr1.tokens[2] is Arr)
            val arr2 = arr1.tokens[2] as Arr
            assertEquals(2, arr2.tokens.size)
            assertLiteral(true, BooleanLiteral::class, arr2.tokens[0])

            run {
                assert(arr2.tokens[1] is Arr)
                val arr3 = arr2.tokens[1] as Arr
                assertEquals(2, arr3.tokens.size)

                run {
                    assert(arr3.tokens[0] is FnToken)
                    val fnToken = arr3.tokens[0] as FnToken
                    assertEquals(3, fnToken.tokens.size)
                    assertLiteral("fn", Identifier::class, fnToken.tokens[0])

                    assert(fnToken.tokens[1] is Arr)
                    val arr4 = fnToken.tokens[1] as Arr
                    assertEquals(1, arr4.tokens.size)
                    assertLiteral("x", Identifier::class, arr4.tokens[0])

                    assertLiteral("x", Identifier::class, fnToken.tokens[2])
                }

                assertLiteral("[rar]", StringLiteral::class, arr3.tokens[1])
            }
        }

        assertLiteral(false, BooleanLiteral::class, arr1.tokens[3])
    }

    @Test
    @Timeout(1)
    fun testNonNestedFunction() {
        val tokens = tokenize("'sup' .23 (map some 1 -2.01 true 'hello there!' ['a' 1]) false")

        assertEquals(4, tokens.size)
        assertLiteral("sup", StringLiteral::class, tokens[0])
        assertLiteral(0.23, DoubleLiteral::class, tokens[1])

        run {
            assert(tokens[2] is FnToken)
            val map = tokens[2] as FnToken
            assertEquals(7, map.tokens.size)
            assertLiteral("map", Identifier::class, map.tokens[0])
            assertLiteral("some", Identifier::class, map.tokens[1])
            assertLiteral(1, IntLiteral::class, map.tokens[2])
            assertLiteral(-2.01, DoubleLiteral::class, map.tokens[3])
            assertLiteral(true, BooleanLiteral::class, map.tokens[4])
            assertLiteral("hello there!", StringLiteral::class, map.tokens[5])

            run {
                val arr = map.tokens[6] as Arr
                assertEquals(2, arr.tokens.size)
                assertLiteral("a", StringLiteral::class, arr.tokens[0])
                assertLiteral(1, IntLiteral::class, arr.tokens[1])
            }
        }

        assertLiteral(false, BooleanLiteral::class, tokens[3])
    }

    @Test
    @Timeout(1)
    fun testNestedFunctions() {
        val tokens = tokenize("(map (fn [x] (let [y 2] (+ x y))) [2 2])")

        assert(tokens[0] is FnToken)
        val map = tokens[0] as FnToken
        assertEquals(3, map.tokens.size)
        assertLiteral("map", Identifier::class, map.tokens[0])

        run {
            assert(map.tokens[1] is FnToken)
            val fnToken = map.tokens[1] as FnToken
            assertEquals(3, fnToken.tokens.size)
            assertLiteral("fn", Identifier::class, fnToken.tokens[0])

            run {
                assert(fnToken.tokens[1] is Arr)
                val arr = fnToken.tokens[1] as Arr
                assertLiteral("x", Identifier::class, arr.tokens[0])
            }

            run {
                assert(fnToken.tokens[2] is FnToken)
                val let = fnToken.tokens[2] as FnToken
                assertEquals(3, let.tokens.size)
                assertLiteral("let", Identifier::class, let.tokens[0])

                run {
                    assert(let.tokens[1] is Arr)
                    val arr2 = let.tokens[1] as Arr
                    assertEquals(2, arr2.tokens.size)
                    assertLiteral("y", Identifier::class, arr2.tokens[0])
                    assertLiteral(2, IntLiteral::class, arr2.tokens[1])
                }

                run {
                    assert(let.tokens[2] is FnToken)
                    val plus = let.tokens[2] as FnToken
                    assertEquals(3, plus.tokens.size)
                    assertLiteral("+", Identifier::class, plus.tokens[0])
                    assertLiteral("x", Identifier::class, plus.tokens[1])
                    assertLiteral("y", Identifier::class, plus.tokens[2])
                }
            }
        }

        assert(map.tokens[2] is Arr)
        val arr3 = map.tokens[2] as Arr
        assertEquals(2, arr3.tokens.size)
        assertLiteral(2, IntLiteral::class, arr3.tokens[0])
        assertLiteral(2, IntLiteral::class, arr3.tokens[1])
    }

    @Test
    @Timeout(1)
    fun testMap() {
        val tokens = tokenize("2.3 {is-cheese? '{no way}' {test [1 2 (fn [x] x)]} false}")

        assertEquals(2, tokens.size)
        assertLiteral(2.3, DoubleLiteral::class, tokens[0])

        assert(tokens[1] is MapToken)
        val mapToken = tokens[1] as MapToken
        assertEquals(4, mapToken.tokens.size)
        assertLiteral("is-cheese?", Identifier::class, mapToken.tokens[0])
        assertLiteral("{no way}", StringLiteral::class, mapToken.tokens[1])

        run {
            assert(mapToken.tokens[2] is MapToken)
            val mapToken2 = mapToken.tokens[2] as MapToken
            assertEquals(2, mapToken2.tokens.size)
            assertLiteral("test", Identifier::class, mapToken2.tokens[0])

            run {
                assert(mapToken2.tokens[1] is Arr)
                val arr = mapToken2.tokens[1] as Arr
                assertEquals(3, arr.tokens.size)
                assertLiteral(1, IntLiteral::class, arr.tokens[0])
                assertLiteral(2, IntLiteral::class, arr.tokens[1])

                run {
                    assert(arr.tokens[2] is FnToken)
                    val fnToken = arr.tokens[2] as FnToken
                    assertEquals(3, fnToken.tokens.size)
                    assertLiteral("fn", Identifier::class, fnToken.tokens[0])

                    run {
                        assert(fnToken.tokens[1] is Arr)
                        val arr2 = fnToken.tokens[1] as Arr
                        assertEquals(1, arr2.tokens.size)
                        assertLiteral("x", Identifier::class, arr2.tokens[0])
                    }

                    assertLiteral("x", Identifier::class, fnToken.tokens[2])
                }
            }
        }

        assertLiteral(false, BooleanLiteral::class, mapToken.tokens[3])
    }

    @Test
    @Timeout(1)
    fun testMultipleLines() {
        val tokens = tokenize("true\nfalse\n(+\n1\n1)")

        assertEquals(3, tokens.size)
        assertLiteral(true, BooleanLiteral::class, tokens[0])
        assertLiteral(false, BooleanLiteral::class, tokens[1])

        assert(tokens[2] is FnToken)
        val plus = tokens[2] as FnToken
        assertEquals(3, plus.tokens.size)
        assertLiteral("+", Identifier::class, plus.tokens[0])
        assertLiteral(1, IntLiteral::class, plus.tokens[1])
        assertLiteral(1, IntLiteral::class, plus.tokens[2])
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
}