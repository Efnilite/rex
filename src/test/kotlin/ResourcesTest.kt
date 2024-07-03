import dev.efnilite.rex.Parser.parse
import dev.efnilite.rex.Scope
import dev.efnilite.rex.Tokenizer.Companion.tokenize
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URI

object ResourcesTest {

    @Test
    fun testResources() {
        val core = this::class.java.getResourceAsStream("core.rx")!!
            .bufferedReader().use { it.readText() }

        val scope = Scope(null)

        parse(tokenize(core), scope)

        for (file in listResourceFiles()) {
            val content = file.readText()
            parse(tokenize(content), scope)
            println("Passed ${file.nameWithoutExtension}")
        }
    }

    private fun listResourceFiles(): List<File> {
        val resource = this::class.java.getResource("tests")
        val dir = File(URI(resource!!.toString()).path)
        return dir.walkTopDown().filter { it.extension == "rx" }.toList()
    }
}