package world.gregs.voidps.engine.data.json

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.AccountStorageTest
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.yaml.Yaml
import java.io.File
import kotlin.test.assertTrue

class FileStorageTest : AccountStorageTest() {

    @field:TempDir
    lateinit var directory: File

    override lateinit var storage: AccountStorage

    private val itemDefinitions = ItemDefinitions(Array(30000) { ItemDefinition.EMPTY }).apply {
        ids = emptyMap()
    }

    @BeforeEach
    fun setup() {
        storage = FileStorage(Yaml(), directory, itemDefinitions, 1.0)
    }

    @Test
    fun `Invalid directory returns no names`() {
        val storage = FileStorage(Yaml(), directory.resolve("invalid"), itemDefinitions, 1.0)
        assertTrue(storage.names().isEmpty())
    }

    @Test
    fun `Invalid directory returns no clans`() {
        val storage = FileStorage(Yaml(), directory.resolve("invalid"), itemDefinitions, 1.0)
        assertTrue(storage.clans().isEmpty())
    }

    @AfterEach
    fun teardown() {
        directory.listFiles()?.forEach { it.delete() }
    }
}