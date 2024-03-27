package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SafeStorageTest {

    @TempDir
    private lateinit var dir: File

    private lateinit var storage: AccountStorage

    @BeforeEach
    fun setup() {
        storage = SafeStorage(dir)
    }

    @Test
    fun `Store an account`() {
        storage.save(listOf(AccountStorageTest.save))
        val files = dir.listFiles()
        assertFalse(files.isNullOrEmpty())
        val file = files!!.first()
        assertTrue(file.name.endsWith("durial_321.json"))
        assertTrue(file.readText().contains("\"accountName\": \"durial_321\","))
    }
}