package rs.dusk.engine.client.ui.dialogue

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.entity.character.npc.NPC

internal class DialogueTest {

    lateinit var manager: Dialogue
    lateinit var io: DialogueIO

    @BeforeEach
    fun setup() {
        io = mockk(relaxed = true)
        manager = Dialogue(io)
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
            await(Dialogue.Type.Chat)
        }

        withContext(Contexts.Game) {
            assertFalse(manager.isEmpty)
        }
    }

    @Test
    fun `Suspended dialogue resumed is empty`() = runBlocking {
        manager.start {
            await(Dialogue.Type.Chat)
        }

        withContext(Contexts.Game) {
            manager.resume()
            assertTrue(manager.isEmpty)
        }
    }

    @Test
    fun `Get current suspension type`() = runBlocking {
        manager.start {
            await(Dialogue.Type.Chat)
        }

        withContext(Contexts.Game) {
            assertEquals(Dialogue.Type.Chat, manager.currentType())
        }
    }

    @Test
    fun `Current suspension null`() {
        assertNull(manager.currentType())
    }

    @Test
    fun `Send builder dialogue`() = runBlocking {
        val npc: NPC = mockk()
        val builder = DialogueBuilder(npc)
        manager.start {
            builder dialogue "text"
        }
        withContext(Contexts.Game) {
            assertEquals("text", builder.text)
            assertEquals(Dialogue.Type.Chat, manager.currentType())
            verify {
                io.sendChat(builder)
            }
        }
    }

    @Test
    fun `Dialogue on any entity`() = runBlocking {
        val npc: NPC = mockk()
        manager.start {
            npc dialogue "Text"
        }

        withContext(Contexts.Game) {
            assertEquals(Dialogue.Type.Chat, manager.currentType())
            verify {
                io.sendChat(any())
            }
        }
    }

    @Test
    fun `Send builder statement`() = runBlocking {
        val npc: NPC = mockk()
        val builder = DialogueBuilder(npc)
        manager.start {
            builder statement "text"
        }
        withContext(Contexts.Game) {
            assertEquals("text", builder.text)
            assertEquals(Dialogue.Type.Statement, manager.currentType())
            verify {
                io.sendStatement(builder)
            }
        }
    }

    @Test
    fun `Statement on any entity`() = runBlocking {
        val npc: NPC = mockk()
        manager.start {
            npc statement "Text"
        }

        withContext(Contexts.Game) {
            assertEquals(Dialogue.Type.Statement, manager.currentType())
            verify {
                io.sendStatement(any())
            }
        }
    }

    @Test
    fun `Send builder choice`() = runBlocking {
        val npc: NPC = mockk()
        val builder = DialogueBuilder(npc)
        manager.start {
            builder choice "text"
        }
        withContext(Contexts.Game) {
            assertEquals("text", builder.text)
            assertEquals(Dialogue.Type.Choice, manager.currentType())
            verify {
                io.sendChoice(builder)
            }
        }
    }

    @Test
    fun `Choice on any entity`() = runBlocking {
        val npc: NPC = mockk()
        manager.start {
            npc choice "Text"
        }

        withContext(Contexts.Game) {
            assertEquals(Dialogue.Type.Choice, manager.currentType())
            verify {
                io.sendChoice(any())
            }
        }
    }

    @Test
    fun `Dialogue builder`() {
        val npc: NPC = mockk()
        val builder = DialogueBuilder(target = npc)
        assertEquals("", builder.text)
        assertEquals(Expression.Talking, builder.expression)
        assertNull(builder.title)
        assertFalse(builder.large)
        assertTrue(builder.clickToContinue)
    }

    @Test
    fun `Animation dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = npc animation Expression.Laugh
            assertEquals(npc, builder.target)
            assertEquals(Expression.Laugh, builder.expression)
        }
    }

    @Test
    fun `Title dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = npc title "text"
            assertEquals(npc, builder.target)
            assertEquals("text", builder.title)
        }
    }

    @Test
    fun `Large dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = npc large true
            assertEquals(npc, builder.target)
            assertTrue(builder.large)
        }
    }

    @Test
    fun `Continue dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = npc clickToContinue false
            assertEquals(npc, builder.target)
            assertFalse(builder.clickToContinue)
        }
    }

    @Test
    fun `Send string entry`() = runBlocking {
        manager.start {
            stringEntry("text", false)
        }
        withContext(Contexts.Game) {
            assertEquals(Dialogue.Type.String, manager.currentType())
            verify {
                io.sendStringEntry("text", false)
            }
        }
    }

    @Test
    fun `Send integer entry`() = runBlocking {
        manager.start {
            intEntry("text")
        }
        withContext(Contexts.Game) {
            assertEquals(Dialogue.Type.Int, manager.currentType())
            verify {
                io.sendIntEntry("text")
            }
        }
    }

    @Test
    fun `Send item destroy`() = runBlocking {
        manager.start {
            destroy("text", 1234)
        }
        withContext(Contexts.Game) {
            assertEquals(Dialogue.Type.Destroy, manager.currentType())
            verify {
                io.sendItemDestroy("text", 1234)
            }
        }
    }
}