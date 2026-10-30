package world.gregs.voidps.web.route

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProxyLimitsTest {

    @Test
    fun `Connections per host are capped`() {
        val limits = ProxyLimits(maxPerHost = 2, maxTotal = 10)
        assertTrue(limits.acquire("1.1.1.1"))
        assertTrue(limits.acquire("1.1.1.1"))
        assertFalse(limits.acquire("1.1.1.1"))
        assertTrue(limits.acquire("2.2.2.2"))
    }

    @Test
    fun `Total connections are capped`() {
        val limits = ProxyLimits(maxPerHost = 5, maxTotal = 2)
        assertTrue(limits.acquire("1.1.1.1"))
        assertTrue(limits.acquire("2.2.2.2"))
        assertFalse(limits.acquire("3.3.3.3"))
    }

    @Test
    fun `Releasing frees up a slot`() {
        val limits = ProxyLimits(maxPerHost = 1, maxTotal = 1)
        assertTrue(limits.acquire("1.1.1.1"))
        limits.release("1.1.1.1")
        assertEquals(0, limits.count("1.1.1.1"))
        assertEquals(0, limits.total())
        assertTrue(limits.acquire("2.2.2.2"))
    }
}
