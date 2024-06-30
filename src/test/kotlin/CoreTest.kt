import dev.efnilite.rex.Parser.parse
import dev.efnilite.rex.Tokenizer.Companion.tokenize
import org.junit.jupiter.api.Test

object CoreTest {

    @Test
    fun testCore() {
        val core = this::class.java.getResourceAsStream("core.rx")!!
            .bufferedReader().use { it.readText() }

        parse(tokenize(core))
    }

}