import dev.efnilite.rex.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

object RTTest {

    @Test
    fun testIss() {
        // TODO
    }

    @Test
    fun testIff() {
        // TODO
    }

    @Test
    fun testCount() {
        // TODO
    }

    @Test
    fun testGet() {
        // TODO
    }

    @Test
    fun testDrop() {
        assertEquals(Arr(3, 4), RT.drop(2, Arr(1, 2, 3, 4)))
        assertEquals(Arr(), RT.drop(2, Arr(1)))
        assertEquals("llo", RT.drop(2, "Hello"))
        assertEquals("", RT.drop(2, "H"))

        assertFails { RT.drop(2, 1) }
        assertFails { RT.drop(-1, Arr()) }
    }

    @Test
    fun testTake() {
        assertEquals(Arr(1, 2), RT.take(2, Arr(1, 2, 3, 4)))
        assertEquals(Arr(1), RT.take(2, Arr(1)))
        assertEquals("He", RT.take(2, "Hello"))
        assertEquals("H", RT.take(2, "H"))

        assertFails { RT.take(2, 1) }
        assertFails { RT.take(-1, Arr()) }
    }

    @Test
    fun testReduce() {
        val fn = AnonymousFn(Arr(Identifier("a"), Identifier("b")),
            listOf(Fn("dev.efnilite.rex.RT/add", listOf(Identifier("a"), Identifier("b")))))

        assertFails { RT.reduce(1, 0, Arr(1, 2, 3)) }
        assertFails { RT.reduce(fn, 0, null) }
        assertFails { RT.reduce(fn, 0, Mp()) }

        assertEquals(6, RT.reduce(fn, 0, Arr(1, 2, 3)))
    }

    @Test
    fun testAdd() {
        assertEquals(3, RT.add(1, 2))
        assertEquals(3.0, RT.add(1.0, 2))
        assertEquals(-1, RT.add(1, -2))
        assertEquals(1.0, RT.add(-1, 2.0))
        assertEquals(-7.0, RT.add("-5", -2.0))
        assertEquals(1.5, RT.add(-3.5, "5.0"))
        assertEquals(10E33, RT.add(5E33, 5E33))
    }

    @Test
    fun testSubtract() {
        assertEquals(-1, RT.subtract(1, 2))
        assertEquals(-1.0, RT.subtract(1.0, 2))
        assertEquals(3, RT.subtract(1, -2))
        assertEquals(-3.0, RT.subtract(1, 4.0))
        assertEquals(3.0, RT.subtract(-1, -4.0))
        assertEquals(-7.0, RT.subtract("-5", 2.0))
        assertEquals(-8.5, RT.subtract(-3.5, "5.0"))
        assertEquals(0.0, RT.subtract(1E30, 1E30))
    }

    @Test
    fun testMultiply() {
        assertEquals(2, RT.multiply(1, 2))
        assertEquals(2.0, RT.multiply(1.0, 2))
        assertEquals(-2, RT.multiply(1, -2))
        assertEquals(-2.0, RT.multiply(1, -2.0))
        assertEquals(-2.0, RT.multiply(-1, 2.0))
        assertEquals(-10.0, RT.multiply("-5", 2.0))
        assertEquals(-17.5, RT.multiply(-3.5, "5.0"))
        assertEquals(1E20, RT.multiply(1E10, 1E10))
    }

    @Test
    fun testDivide() {
        assertEquals(0.5, RT.divide(1, 2))
        assertEquals(0.5, RT.divide(1.0, 2))
        assertEquals(-0.5, RT.divide(1, -2))
        assertEquals(-0.5, RT.divide(1, -2.0))
        assertEquals(-0.5, RT.divide(-1, 2.0))
        assertEquals(-2.5, RT.divide("-5", 2.0))
        assertEquals(-0.7, RT.divide(-3.5, "5.0"))
        assertEquals(10.0, RT.divide(5E33, 5E32))
    }
}