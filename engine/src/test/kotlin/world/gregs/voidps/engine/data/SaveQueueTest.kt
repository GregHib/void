package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.exchange.Claim
import world.gregs.voidps.engine.data.exchange.OpenOffers
import world.gregs.voidps.engine.data.exchange.PriceHistory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.script.KoinMock
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
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
    fun `Failed save falls back and doesn't kill the queue`() {
        val fallbackSaved = CountDownLatch(1)
        val saved = CountDownLatch(1)
        var fail = true
        val storage = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                if (fail) {
                    throw IOException("Disk full")
                }
                saved.countDown()
            }
        }
        val fallback = object : TestStorage() {
            override fun save(accounts: List<PlayerSave>) {
                fallbackSaved.countDown()
            }
        }
        val queue = SaveQueue(storage, fallback)
        queue.save(Player(accountName = "player"))
        queue.run()
        assertTrue(fallbackSaved.await(5, TimeUnit.SECONDS), "Fallback didn't run after failed save")
        waitFor("fallback to clear pending") { queue.empty() }
        fail = false
        queue.save(Player(accountName = "player"))
        waitFor("save after a failure") {
            queue.run()
            saved.count == 0L
        }
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

    private fun waitFor(description: String, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5000
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "Timed out waiting for $description" }
            Thread.sleep(10)
        }
    }
}
