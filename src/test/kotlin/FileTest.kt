import dev.efnilite.rex.Scope
import dev.efnilite.rex.parse
import dev.efnilite.rex.tokenize
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URI

object FileTest {

    @Test
    fun testFiles() {
        val core = this::class.java.getResourceAsStream("core.rx")!!
            .bufferedReader().use { it.readText() }

        val scope = Scope(null)

        parse(tokenize(core), scope)

        println("Running file tests...")
        for (file in listResourceFiles()) {
            val content = file.readText()
            try {
                parse(tokenize(content), scope)
                println("Passed tests in ${file.nameWithoutExtension}")
            } catch (e: Exception) {
                println("Failed tests in ${file.nameWithoutExtension}")
                throw e
            }
        }
    }

    private fun listResourceFiles(): List<File> {
        val resource = this::class.java.getResource("tests")
        val dir = File(URI(resource!!.toString()).path)
        return dir.walkTopDown().filter { it.extension == "rx" }.toList()
    }
}