package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SafeStorageTest {

    @TempDir
    private lateinit var dir: File

    private lateinit var storage: Storage

    @BeforeEach
    fun setup() {
        storage = SafeStorage(dir)
    }

    @Test
    fun `Store an account`() {
        storage.save(listOf(StorageTest.save))
        val files = dir.listFiles()
        assertFalse(files.isNullOrEmpty())
        val file = files!!.first()
        assertTrue(file.name.endsWith("durial_321.toml"))
        val expected = File("./src/test/resources/player.toml").readText().replace("\r\n", "\n")
        assertEquals(expected, file.readText().replace("\r\n", "\n"))
    }

    @Test
    fun `Store an abuse report`() {
        val report = AbuseReport(
            reporter = "mod_steve",
            reported = "Durial321",
            rule = 6,
            ruleName = "Macroing",
            mute = true,
            suggestion = "extra info",
            time = 1234567890,
            evidence = listOf("[00:00:01] public: free armour trimming"),
        )

        storage.saveReport(report)

        val file = dir.resolve("reports/1234567890-mod_steve.toml")
        assertTrue(file.exists())
        val expected = """
            reporter = "mod_steve"
            reported = "Durial321"
            rule = 6
            rule_name = "Macroing"
            mute = true
            suggestion = "extra info"
            time = 1234567890
            evidence = ["[00:00:01] public: free armour trimming"]
        """.trimIndent()
        assertEquals(expected, file.readText().replace("\r\n", "\n").trim())
    }
}
