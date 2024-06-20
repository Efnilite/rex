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

}