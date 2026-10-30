package world.gregs.voidps.engine.entity.obj

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameObjectTimersTest {

    private lateinit var timers: GameObjectTimers

    @BeforeEach
    fun setup() {
        timers = GameObjectTimers()
    }

    @Test
    fun `Block invoked after ticks`() {
        val obj = GameObject(12345)
        var invoked = false
        timers.add(obj, 5) {
            invoked = true
        }
        repeat(5) {
            assertFalse(invoked)
            timers.run()
        }
        assertTrue(invoked)
    }

    @Test
    fun `Canceled timer is never invoked`() {
        val obj = GameObject(12345)
        var invoked = false
        timers.add(setOf(GameObject(54321), obj), 5) {
            invoked = true
        }
        assertFalse(invoked)
        timers.run()
        timers.cancel(obj)
        assertFalse(invoked)
        repeat(5) { timers.run() }
        assertFalse(invoked)
    }

    @Test
    fun `Executed timer is removed`() {
        val obj = GameObject(12345)
        var invoked = 0
        timers.add(setOf(GameObject(54321), obj), 5) {
            invoked++
        }
        assertEquals(0, invoked)
        timers.run()
        timers.execute(obj)
        assertEquals(1, invoked)
        repeat(5) { timers.run() }
        assertEquals(1, invoked)
    }

    @Test
    fun `Block can add and cancel timers while running`() {
        val obj = GameObject(12345)
        val other = GameObject(54321)
        var added = 0
        timers.add(other, 2) { added = -1 }
        timers.add(obj, 1) {
            timers.cancel(other)
            timers.add(obj, 2) { added++ }
        }
        timers.run()
        assertEquals(0, added)
        repeat(2) { timers.run() }
        assertEquals(1, added)
    }
}
