package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SuspensionSystemTest {

    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = Player()
        GameLoop.tick = 0
    }

    @Test
    fun `Int suspends until integer is provided`() = runTest {
        var result: Int? = null

        // Launch the suspending function in a child coroutine
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            result = player.pauseInt()
        }

        // Assert the coroutine is suspended and waiting for an Int
        assertTrue(player.suspension is Suspension.IntEntry)
        assertNull(result, "Should not return a result yet")

        // Provide the input
        val intSuspension = player.suspension as Suspension.IntEntry
        intSuspension.resume(42)

        // Assert resumption was successful
        assertEquals(42, result)
        assertNull(player.suspension, "Suspension should be cleared after resume")
        job.cancel()
    }

    @Test
    fun `pauseString suspends until string is provided`() = runTest {
        var result: String? = null

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            result = player.pauseString()
        }

        assertTrue(player.suspension is Suspension.StringEntry)

        (player.suspension as Suspension.StringEntry).resume("Hello World")

        assertEquals("Hello World", result)
        assertNull(player.suspension)
        job.cancel()
    }

    @Test
    fun `pauseButton suspends until continue is clicked`() = runTest {
        var resumed = false

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            player.pauseButton()
            resumed = true
        }

        assertTrue(player.suspension is Suspension.Continue)
        assertFalse(resumed)

        (player.suspension as Suspension.Continue).resume()

        assertTrue(resumed)
        assertNull(player.suspension)
        job.cancel()
    }


    @Test
    fun `Delay suspension respects GameLoop ticks`() = runTest {
        var resumed = false

        // Set a delay of 5 ticks
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            kotlinx.coroutines.suspendCancellableCoroutine {
                player.suspension = Suspension.Delay(it, 5)
            }
            resumed = true
        }

        // Verify it's waiting
        val delaySuspension = player.suspension as Suspension.Delay
        assertEquals(5, delaySuspension.tick)

        // Advance 4 ticks (should not be ready)
        GameLoop.tick = 4
        assertFalse(delaySuspension.ready())
        player.resumeSuspension()
        assertFalse(resumed)

        // Advance to 5 ticks (should be ready)
        GameLoop.tick = 5
        assertTrue(delaySuspension.ready())

        // Call the periodic checker
        player.resumeSuspension()

        // Verify resumption
        assertTrue(resumed)
        assertNull(player.suspension)
        job.cancel()
    }

    @Test
    fun `AwaitDialogues waits until dialogue is null`() = runTest {
        var resumed = false
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(id = 0, type = "dialogue_box")), mapOf("dialog" to 0))
        player.interfaces = Interfaces(player, mutableMapOf())
        player.open("dialog")

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            player.awaitDialogues()
            resumed = true
        }

        val customSuspension = player.suspension as Suspension.Custom

        // Should not be ready while dialogue is present
        assertFalse(customSuspension.ready())
        player.resumeSuspension()
        assertFalse(resumed)

        // Clear dialogue, should now be ready
        player.close("dialog")
        assertTrue(customSuspension.ready())

        player.resumeSuspension()
        assertTrue(resumed)
        job.cancel()
    }

    @Test
    fun `ResumeSuspension returns false if no suspension exists`() {
        player.suspension = null
        assertFalse(player.resumeSuspension())
    }

    @Test
    fun `ResumeSuspension returns true even if delay is not ready`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)
        testScope.launch {
            suspendCancellableCoroutine {
                player.suspension = Suspension.Delay(it, 10)
            }
        }
        delay(5.milliseconds)

        // Not ready yet, but resumeSuspension returns true indicating a suspension exists
        assertTrue(player.resumeSuspension())
        assertNotNull(player.suspension) // Should not have cleared it
    }
}