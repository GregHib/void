package content.social.trade.exchange.offer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OffersTest {

    @TempDir
    lateinit var buyDirectory: File

    @TempDir
    lateinit var sellDirectory: File

    @Test
    fun `Loading buy offers from file`() {
        val file = buyDirectory.resolve("tinderbox.toml")
        file.createNewFile()
        file.writeText("""
            [1]
            amount = 100
            price = 200
            state = 2
            last_active = 1000001
            completed = 100
            account = "bob"
        """.trimIndent())
        val offers = Offers()
        offers.load(buyDirectory, sellDirectory, 1)
        val offer = offers.offer(1)
        assertNotNull(offer)
        assertEquals("tinderbox", offer.item)
        assertEquals(100, offer.amount)
        assertEquals(200, offer.price)
        assertFalse(offer.sell)
        assertEquals(OfferState.OpenBuy, offer.state)
        assertEquals(1000001L, offer.lastActive)
        assertEquals(100, offer.completed)
        assertEquals("bob", offer.account)
    }

    @Test
    fun `Loading sell offers from file`() {
        val file = sellDirectory.resolve("tinderbox.toml")
        file.createNewFile()
        file.writeText("""
            [1]
            amount = 100
            price = 200
            state = 10
            last_active = 1000001
            completed = 100
            account = "bob"
        """.trimIndent())
        val offers = Offers()
        offers.load(buyDirectory, sellDirectory, 1)
        val offer = offers.offer(1)
        assertNotNull(offer)
        assertEquals("tinderbox", offer.item)
        assertEquals(100, offer.amount)
        assertEquals(200, offer.price)
        assertEquals(OfferState.OpenSell, offer.state)
        assertEquals(1000001L, offer.lastActive)
        assertEquals(100, offer.completed)
        assertEquals("bob", offer.account)
    }

    @Test
    fun `Saving sale to file`() {
        val offer = Offer(id = 1, item = "tinderbox", amount = 100, price = 200, state = OfferState.OpenSell, lastActive = 1000000, completed = 100, account = "bob")
        val offers = Offers()
        offers.sell(offer)
        offers.save(buyDirectory, sellDirectory)

        assertNotNull(offer)
        assertEquals("""
            [1]
            amount = 100
            price = 200
            state = 10
            last_active = 1000000
            completed = 100
            account = "bob"
            
        """.trimIndent(), sellDirectory.resolve("tinderbox.toml").readText())
    }

    @Test
    fun `Saving buy offer to file`() {
        val offer = Offer(id = 1, item = "tinderbox", amount = 100, price = 200, state = OfferState.OpenBuy, lastActive = 1000000, completed = 100, account = "bob")
        val offers = Offers()
        offers.buy(offer)
        offers.save(buyDirectory, sellDirectory)

        assertNotNull(offer)
        assertEquals("""
            [1]
            amount = 100
            price = 200
            state = 2
            last_active = 1000000
            completed = 100
            account = "bob"
            
        """.trimIndent(), buyDirectory.resolve("tinderbox.toml").readText())
    }

}