package world.gregs.voidps.web

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class PeriodicSnapshotTest {

    private var now = 0L
    private var builds = 0
    private val pending = mutableListOf<Runnable>()

    private fun snapshot(build: () -> Int = { ++builds }) = PeriodicSnapshot("test", refreshMillis = 1000, clock = { now }, refresh = { pending.add(it) }, build = build)

    @Test
    fun `First get builds synchronously`() {
        val snapshot = snapshot()

        assertEquals(1, snapshot.get())
        assertEquals(1, builds)
        assertEquals(0, pending.size)
    }

    @Test
    fun `Fresh value isn't rebuilt`() {
        val snapshot = snapshot()
        snapshot.get()
        now = 999

        assertEquals(1, snapshot.get())
        assertEquals(1, builds)
        assertEquals(0, pending.size)
    }

    @Test
    fun `Stale value is served while one rebuild runs in the background`() {
        val snapshot = snapshot()
        snapshot.get()
        now = 1000

        assertEquals(1, snapshot.get())
        assertEquals(1, snapshot.get())
        assertEquals(1, pending.size)

        pending.removeFirst().run()

        assertEquals(2, snapshot.get())
        assertEquals(2, builds)
    }

    @Test
    fun `Failed rebuild keeps the previous value and retries`() {
        var fail = false
        val snapshot = snapshot { if (fail) error("Failed") else ++builds }
        snapshot.get()
        now = 1000
        fail = true
        snapshot.get()
        pending.removeFirst().run()

        assertEquals(1, snapshot.get())
        assertEquals(1, pending.size)

        fail = false
        pending.removeFirst().run()
        assertEquals(2, snapshot.get())
    }

    @Test
    fun `Failed first build throws`() {
        val snapshot = snapshot { error("Failed") }

        assertThrows<IllegalStateException> { snapshot.get() }
    }
}
