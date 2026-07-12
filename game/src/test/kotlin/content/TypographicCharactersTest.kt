package content

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class TypographicCharactersTest {

    /**
     * Interface option matchers compare against cache strings, which may legitimately
     * contain non-ASCII characters - they are never written to the client.
     */
    private val allowed = setOf(
        "TradeLending.kt",
    )

    /**
     * Writer.writeString sends each character as a single byte, so anything outside the
     * client's single-byte charset (smart quotes, ellipses, em-dashes) truncates to a
     * control byte the client's font handling can't render, crashing the game.
     */
    @Test
    fun `Client-bound strings only use single-byte characters`() {
        val root = sequenceOf(File("src/main/kotlin/content"), File("game/src/main/kotlin/content")).firstOrNull { it.isDirectory }
        assertTrue(root != null, "Unable to locate the content source directory.")
        val offences = mutableListOf<String>()
        for (file in root!!.walkTopDown().filter { it.extension == "kt" && it.name !in allowed }) {
            var blockComment = false
            for ((index, raw) in file.readLines().withIndex()) {
                var line = raw
                if (blockComment) {
                    val end = line.indexOf("*/")
                    if (end == -1) {
                        continue
                    }
                    line = line.substring(end + 2)
                    blockComment = false
                }
                while (true) {
                    val start = line.indexOf("/*")
                    if (start == -1) {
                        break
                    }
                    val end = line.indexOf("*/", start + 2)
                    if (end == -1) {
                        line = line.substring(0, start)
                        blockComment = true
                        break
                    }
                    line = line.substring(0, start) + line.substring(end + 2)
                }
                val comment = line.indexOf("//")
                if (comment != -1) {
                    line = line.substring(0, comment)
                }
                if (line.any { it.code > 0xFF }) {
                    offences.add("${file.relativeTo(root)}:${index + 1}: ${raw.trim()}")
                }
            }
        }
        assertTrue(offences.isEmpty(), "Multi-byte characters found in client-bound strings:\n${offences.joinToString("\n")}")
    }
}
