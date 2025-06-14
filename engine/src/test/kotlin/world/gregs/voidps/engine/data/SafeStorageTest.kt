package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Assertions.*
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
        assertTrue(file.name.endsWith("durial_321.toml"))
        val expected = File("./src/test/resources/player.toml").readText().replace("\r\n", "\n")
        assertEquals(expected, file.readText().replace("\r\n", "\n"))
    }
}
