package rs.dusk.engine.client.ui.dialogue

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.player.Player

internal class DialogueTest {

    lateinit var manager: Dialogues
    lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        manager = spyk(Dialogues())
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
            println("Clear")
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
            assertEquals(-1, npcId)
            assertEquals("", npcName)
        }
    }

    @Test
    fun `Start with npc id and name`() {
        manager.start(player, 123, "Jim") {
            assertEquals(123, npcId)
            assertEquals("Jim", npcName)
        }
    }

    @Test
    fun `Start with npc`() {
        val npc: NPC = mockk()
        every { npc.id } returns 123
        every { npc.def.name } returns "Jim"
        manager.start(player, npc) {
            assertEquals(123, npcId)
            assertEquals("Jim", npcName)
        }
    }

    @Test
    fun `Start dialogue extension`() {
        player.dialogue {
            assertEquals(-1, npcId)
            assertEquals("", npcName)
        }
    }

    @Test
    fun `Start with npc id and name extension`() {
        player.dialogue(123, "Jim") {
            assertEquals(123, npcId)
            assertEquals("Jim", npcName)
        }
    }

    @Test
    fun `Start with npc extension`() {
        val npc: NPC = mockk()
        every { npc.id } returns 123
        every { npc.def.name } returns "Jim"
        player.dialogue(npc) {
            assertEquals(123, npcId)
            assertEquals("Jim", npcName)
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
    fun `Resume empty dialogues throws null pointer`() {
        assertThrows<NullPointerException> {
            manager.resume()
        }
    }

}