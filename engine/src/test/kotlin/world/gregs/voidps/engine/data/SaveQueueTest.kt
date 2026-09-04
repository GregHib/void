package world.gregs.voidps.engine.data

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.exchange.Claim
import world.gregs.voidps.engine.data.exchange.OpenOffers
import world.gregs.voidps.engine.data.exchange.PriceHistory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.type.Tile
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SaveQueueTest : KoinMock() {

    private open class TestStorage : Storage {
        override fun names(): Map<String, AccountDefinition> = emptyMap()

        override fun clans(): Map<String, Clan> = emptyMap()

        override fun save(accounts: List<PlayerSave>) {
        }

        override fun offers(days: Int): OpenOffers = OpenOffers()

        override fun saveOffers(offers: OpenOffers) {
        }

        override fun claims(): Map<Int, Claim> = emptyMap()

        override fun saveClaims(claims: Map<Int, Claim>) {
        }

        override fun priceHistory(): Map<String, PriceHistory> = emptyMap()

        override fun savePriceHistory(history: Map<String, PriceHistory>) {
        }

        override fun saveReport(report: AbuseReport) {
        }

        override fun exists(accountName: String): Boolean = false

        override fun load(accountName: String): PlayerSave? = null
    }

    @Test
    fun `Failed save retries against real storage and doesn't kill the queue`() {
        val attempted = AtomicInteger()
        val saved = CountDownLatch(1)
        var fail = true
        val storage = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                attempted.incrementAndGet()
                if (fail) {
                    throw IOException("Disk full")
                }
                saved.countDown()
            }
        }
        val dumped = CopyOnWriteArrayList<String>()
        val fallback = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                accounts.mapTo(dumped) { it.name }
            }
        }
        val queue = SaveQueue(storage, fallback)
        queue.save(Player(accountName = "player"))
        queue.run()
        waitFor("first attempt") { attempted.get() >= 1 }
        fail = false
        waitFor("retry to succeed") {
            queue.run()
            saved.count == 0L
        }
        waitFor("pending to drain") { queue.empty() }
        assertTrue(dumped.isEmpty(), "Transient failure gave up on the first attempt")
    }

    @Test
    fun `Failure only touches the accounts that were attempted`() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val dumped = CopyOnWriteArrayList<String>()
        val storage = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                started.countDown()
                release.await(5, TimeUnit.SECONDS)
                throw IOException("Disk full")
            }
        }
        val fallback = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                accounts.mapTo(dumped) { it.name }
            }
        }
        val queue = SaveQueue(storage, fallback, retryMillis = 0)
        queue.save(Player(accountName = "attempted"))
        queue.run()
        assertTrue(started.await(5, TimeUnit.SECONDS), "Save didn't start")
        queue.save(Player(accountName = "queued_later"))
        release.countDown()
        waitFor("attempted account to be dumped") { dumped.contains("attempted") }
        assertFalse(dumped.contains("queued_later"), "Failure dumped an account storage was never asked to write")
        assertTrue(queue.saving("queued_later"), "Failure dropped an account storage was never asked to write")
    }

    @Test
    fun `Shutdown save writes to the fallback before the job completes`() {
        val dumped = CopyOnWriteArrayList<String>()
        val storage = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                throw IOException("Disk full")
            }
        }
        val fallback = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                accounts.mapTo(dumped) { it.name }
            }
        }
        val queue = SaveQueue(storage, fallback)
        queue.save(Player(accountName = "player"))

        runBlocking { queue.direct().join() }

        assertTrue(dumped.contains("player"), "Shutdown left a failed save nowhere on disk")
    }

    @Test
    fun `Only one save in flight at a time`() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val calls = AtomicInteger()
        val storage = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                calls.incrementAndGet()
                started.countDown()
                release.await(5, TimeUnit.SECONDS)
            }
        }
        val queue = SaveQueue(storage)
        queue.save(Player(accountName = "player"))
        queue.run()
        assertTrue(started.await(5, TimeUnit.SECONDS), "First save didn't start")
        queue.run()
        queue.run()
        assertEquals(1, calls.get(), "Ticks launched overlapping saves")
        assertTrue(queue.saving("player"), "Account not pending while save in flight")
        release.countDown()
        queue.save(Player(accountName = "player2"))
        waitFor("second save after first completes") {
            queue.run()
            calls.get() == 2
        }
        waitFor("pending to drain") { queue.empty() }
    }

    @Test
    fun `Save queued during a write isn't dropped`() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val blocked = AtomicBoolean(true)
        val written = CopyOnWriteArrayList<Tile>()
        val storage = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                accounts.mapTo(written) { it.tile }
                if (!blocked.getAndSet(false)) {
                    return
                }
                started.countDown()
                release.await(5, TimeUnit.SECONDS)
            }
        }
        val queue = SaveQueue(storage)
        queue.save(Player(accountName = "player", tile = Tile(1, 1)))
        queue.run()
        assertTrue(started.await(5, TimeUnit.SECONDS), "First save didn't start")
        queue.save(Player(accountName = "player", tile = Tile(2, 2)))
        release.countDown()
        waitFor("newer snapshot to be written") {
            queue.run()
            written.contains(Tile(2, 2))
        }
        waitFor("pending to drain") { queue.empty() }
    }

    @Test
    fun `Completed save clears pending when nothing superseded it`() {
        val written = CopyOnWriteArrayList<String>()
        val storage = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                accounts.mapTo(written) { it.name }
            }
        }
        val queue = SaveQueue(storage)
        queue.save(Player(accountName = "player"))
        waitFor("save to complete") {
            queue.run()
            written.contains("player")
        }
        waitFor("pending to drain") { queue.empty() }
        assertFalse(queue.saving("player"))
    }

    @Test
    fun `Shutdown save includes accounts pending from a logout`() {
        val written = CopyOnWriteArrayList<String>()
        val storage = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                accounts.mapTo(written) { it.name }
            }
        }
        val queue = SaveQueue(storage)
        queue.save(Player(accountName = "logged_out_player"))

        runBlocking { queue.direct().join() }

        assertTrue(written.contains("logged_out_player"), "Shutdown dropped a save left pending by a logout")
    }

    private fun waitFor(description: String, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5000
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "Timed out waiting for $description" }
            Thread.sleep(10)
        }
    }
}
