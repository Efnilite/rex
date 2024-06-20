import dev.efnilite.rex.RT
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

object RTTest {

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