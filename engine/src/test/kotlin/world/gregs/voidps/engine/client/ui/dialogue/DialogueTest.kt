/*
package world.gregs.voidps.engine.client.ui.dialogue

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.ui.closeType
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

internal class DialogueTest {

    lateinit var manager: Dialogues
    lateinit var player: Player
    lateinit var continuation: Continuation<Any>

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        continuation = object : Continuation<Any> {
            override val context: CoroutineContext
                get() = UnconfinedTestDispatcher()

            override fun resumeWith(result: Result<Any>) {
            }
        }
        manager = spyk(Dialogues(continuation))
    }

    @Test
    fun `Dialogue queue is empty`() {
        assertTrue(manager.isEmpty)
    }

    @Test
    fun `Unsuspended dialogue is empty`() {
        manager.start(player) {}
        assertTrue(manager.isEmpty)
    }

    @Test
    fun `Suspended dialogue isn't empty`() {
        manager.start(player) {
            await("chat")
        }

        runBlocking(Contexts.Game) {
            assertFalse(manager.isEmpty)
        }
    }

    @Test
    fun `Add suspended dialogue`() {
        manager.add(DialogueContext(manager, player))
        assertFalse(manager.isEmpty)
    }

    @Test
    fun `Clear all dialogues`() {
        manager.add(DialogueContext(manager, player))
        manager.add(DialogueContext(manager, player))
        manager.clear()
        assertTrue(manager.isEmpty)
    }

    @Test
    fun `Clear throws cancellation exception`() {
        var cancelled = false
        manager.start(player) {
            try {
                await<Unit>("chat")
            } catch (e: CancellationException) {
                cancelled = true
            }
        }

        runBlocking(Contexts.Game) {
            manager.clear()
        }
        runBlocking(Contexts.Game) {
            assertTrue(cancelled)
        }
    }

    @Test
    fun `Suspended dialogue resumed is empty`() {
        manager.start(player) {
            await("chat")
        }

        runBlocking(Contexts.Game) {
            manager.resume()
            assertTrue(manager.isEmpty)
        }
    }

    @Test
    fun `Get current suspension type`() {
        manager.start(player) {
            await("chat")
        }

        runBlocking(Contexts.Game) {
            assertEquals("chat", manager.currentType())
        }
    }

    @Test
    fun `Start dialogue`() {
        manager.start(player) {
            assertEquals("", npcId)
            assertEquals("", title)
        }
    }

    @Test
    fun `Start with npc id and name`() {
        manager.start(player, "jim", "Jim") {
            assertEquals("jim", npcId)
            assertEquals("Jim", title)
        }
    }

    @Test
    fun `Start with npc`() {
        val npc: NPC = mockk()
        every { npc.id } returns "jim"
        every { npc.def.name } returns "Jim"
        every { npc.values } returns Values()
        manager.start(player, npc) {
            assertEquals("jim", npcId)
            assertEquals("Jim", title)
        }
    }

    @Test
    fun `Start dialogue extension`() {
        player.dialogue {
            assertEquals("", npcId)
            assertEquals("", title)
        }
    }

    @Test
    fun `Start with npc id and name extension`() {
        player.dialogue("jim", "Jim") {
            assertEquals("jim", npcId)
            assertEquals("Jim", title)
        }
    }

    @Test
    fun `Start with npc extension`() {
        val npc: NPC = mockk()
        every { npc.id } returns "jim"
        every { npc.def.name } returns "Jim"
        player.dialogue(npc) {
            assertEquals("jim", npcId)
            assertEquals("Jim", title)
        }
    }

    @Test
    fun `Current suspension null`() {
        assertTrue(manager.currentType().isBlank())
    }

    @Test
    fun `Resume active dialogue`() {
        var resumed = false
        manager.start(player) {
            await<Unit>("test")
            resumed = true
        }
        runBlocking(Contexts.Game) {
            manager.resume()
        }
        runBlocking(Contexts.Game) {
            assertTrue(resumed)
        }
    }

    @Test
    fun `Resuming from suspension clears dialogue interfaces`() {
        manager.start(player) {
            await<Unit>("test")
        }
        runBlocking(Contexts.Game) {
            manager.resume()
        }
        runBlocking(Contexts.Game) {
            verify {
                player.closeType("dialogue_box")
                player.closeType("dialogue_box_small")
            }
        }
    }

    @Test
    fun `Resume empty dialogues throws null pointer`() {
        assertThrows<NullPointerException> {
            manager.resume()
        }
    }

}*/
