package world.gregs.voidps.engine.data

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.definition.AccountDefinitions

class AccountDefinitionsReloaderTest {

    private lateinit var storage: Storage
    private lateinit var definitions: AccountDefinitions
    private lateinit var saveQueue: SaveQueue
    private lateinit var reloader: AccountDefinitionsReloader

    @BeforeEach
    fun setUp() {
        storage = mockk(relaxed = true)
        definitions = AccountDefinitions()
        saveQueue = SaveQueue(storage)
        reloader = AccountDefinitionsReloader(storage, definitions, saveQueue, io = Dispatchers.Unconfined, game = Dispatchers.Unconfined)
    }

    @Test
    fun `Reload merges storage data into definitions`() {
        every { storage.names() } returns mapOf("account" to AccountDefinition("account", "Name", "", "hash"))
        every { storage.clans() } returns emptyMap()
        var completed = -1

        val started = reloader.reload { count -> completed = count }

        assertTrue(started)
        assertEquals(1, completed)
        assertEquals("hash", definitions.get("account")?.passwordHash)
    }

    @Test
    fun `Reload failure keeps old data and allows retry`() {
        definitions.merge(mapOf("account" to AccountDefinition("account", "Name", "", "old_hash")), emptyMap()) { false }
        every { storage.names() } throws IllegalStateException("Storage unavailable")

        assertTrue(reloader.reload())

        assertEquals("old_hash", definitions.get("account")?.passwordHash)
        every { storage.names() } returns mapOf("account" to AccountDefinition("account", "Name", "", "new_hash"))
        every { storage.clans() } returns emptyMap()
        assertTrue(reloader.reload())
        assertEquals("new_hash", definitions.get("account")?.passwordHash)
    }

    @Test
    fun `Reload skips accounts with pending saves`() {
        definitions.merge(mapOf("account" to AccountDefinition("account", "Name", "", "memory_hash")), emptyMap()) { false }
        every { storage.names() } returns mapOf("account" to AccountDefinition("account", "Name", "", "storage_hash"))
        every { storage.clans() } returns emptyMap()
        val pendingQueue = mockk<SaveQueue>()
        every { pendingQueue.saving("account") } returns true
        val reloader = AccountDefinitionsReloader(storage, definitions, pendingQueue, io = Dispatchers.Unconfined, game = Dispatchers.Unconfined)

        var completed = -1
        assertTrue(reloader.reload { count -> completed = count })

        assertEquals(0, completed)
        assertEquals("memory_hash", definitions.get("account")?.passwordHash)
    }

    @Test
    fun `Reload returns false while already in progress`() {
        val blocked = java.util.concurrent.CountDownLatch(1)
        every { storage.names() } answers {
            blocked.await()
            emptyMap()
        }
        every { storage.clans() } returns emptyMap()
        val reloader = AccountDefinitionsReloader(storage, definitions, saveQueue, io = Dispatchers.IO, game = Dispatchers.Unconfined)

        assertTrue(reloader.reload())
        assertFalse(reloader.reload())
        blocked.countDown()
    }
}
