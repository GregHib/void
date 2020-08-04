package rs.dusk.engine.client.ui.dialogue

import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.action.Contexts

internal class DialogueTest {

    lateinit var manager: Dialogues

    @BeforeEach
    fun setup() {
        manager = spyk(Dialogues(mockk()))
    }

    @Test
    fun `Dialogue queue is empty`() {
        assertTrue(manager.isEmpty)
    }

    @Test
    fun `Unsuspended dialogue is empty`() {
        manager.start {}
        assertTrue(manager.isEmpty)
    }

    @Test
    fun `Suspended dialogue isn't empty`() = runBlocking {
        manager.start {
            await("chat")
        }

        withContext(Contexts.Game) {
            assertFalse(manager.isEmpty)
        }
    }

    @Test
    fun `Suspended dialogue resumed is empty`() = runBlocking {
        manager.start {
            await("chat")
        }

        withContext(Contexts.Game) {
            manager.resume()
            assertTrue(manager.isEmpty)
        }
    }

    @Test
    fun `Get current suspension type`() = runBlocking {
        manager.start {
            await("chat")
        }

        withContext(Contexts.Game) {
            assertEquals("chat", manager.currentType())
        }
    }

    @Test
    fun `Current suspension null`() {
        assertTrue(manager.currentType().isBlank())
    }

    @Test
    fun `Resume active dialogue`() = runBlocking {
        var resumed = false
        manager.start {
            await<Unit>("test")
            resumed = true
        }
        withContext(Contexts.Game) {
            manager.resume()
        }
        withContext(Contexts.Game) {
            assertTrue(resumed)
        }
    }

    @Test
    fun `Resume empty dialogues throws null pointer`() {
        assertThrows<NullPointerException> {
            manager.resume()
        }
    }

}