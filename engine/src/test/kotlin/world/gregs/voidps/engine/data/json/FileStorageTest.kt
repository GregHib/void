package world.gregs.voidps.engine.data.json

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.AccountStorageTest
import java.io.File
import kotlin.test.assertTrue

class FileStorageTest : AccountStorageTest() {

    @field:TempDir
    lateinit var directory: File

    override lateinit var storage: AccountStorage

    @BeforeEach
    fun setup() {
        storage = FileStorage(directory)
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