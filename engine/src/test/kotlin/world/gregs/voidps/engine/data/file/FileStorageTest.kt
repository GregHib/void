package world.gregs.voidps.engine.data.file

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.engine.data.AbuseReport
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.StorageTest
import java.io.File
import kotlin.test.assertTrue

class FileStorageTest : StorageTest() {

    @field:TempDir
    lateinit var directory: File

    override lateinit var storage: Storage

    @BeforeEach
    fun setup() {
        storage = FileStorage(directory)
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
            evidence = listOf("[00:00:01] public: free armour trimming", "[00:00:02] public: selling gf"),
        )

        storage.saveReport(report)

        val file = directory.resolve("reports/1234567890-mod_steve.toml")
        assertTrue(file.exists())
        val text = file.readText()
        assertTrue(text.contains("reporter = \"mod_steve\""))
        assertTrue(text.contains("reported = \"Durial321\""))
        assertTrue(text.contains("rule = 6"))
        assertTrue(text.contains("rule_name = \"Macroing\""))
        assertTrue(text.contains("mute = true"))
        assertTrue(text.contains("suggestion = \"extra info\""))
        assertTrue(text.contains("time = 1234567890"))
        assertTrue(text.contains("free armour trimming"))
        assertTrue(text.contains("selling gf"))
    }

    @Test
    fun `Invalid directory returns no names`() {
        val storage = FileStorage(directory.resolve("invalid"))
        assertTrue(storage.names().isEmpty())
    }

    @Test
    fun `Invalid directory returns no clans`() {
        val storage = FileStorage(directory.resolve("invalid"))
        assertTrue(storage.clans().isEmpty())
    }

    @AfterEach
    fun teardown() {
        directory.listFiles()?.forEach { it.delete() }
    }
}
