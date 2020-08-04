package rs.dusk.engine.client.ui.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.entity.character.npc.NPC

internal class DialogueTest {

    lateinit var manager: Dialogues
    lateinit var io: DialogueIO

    @BeforeEach
    fun setup() {
        io = mockk(relaxed = true)
        manager = spyk(Dialogues(io, mockk()))
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
    fun `Send builder dialogue`() = runBlocking {
        every { io.sendChat(any()) } returns true
        val npc: NPC = mockk()
        val builder = DialogueBuilder(npc)
        manager.start {
            builder dialogue "text"
        }
        withContext(Contexts.Game) {
            assertEquals("text", builder.text)
            assertEquals("chat", manager.currentType())
            verify {
                io.sendChat(builder)
            }
        }
    }

    @Test
    fun `Don't await dialogue if failed to send`() = runBlocking {
        val npc: NPC = mockk()
        val builder = DialogueBuilder(npc)
        every { io.sendChat(builder) } returns false
        coEvery { manager.await<Any>(any()) } returns mockk()
        manager.start {
            builder dialogue "text"
        }
        withContext(Contexts.Game) {
            coVerify(exactly = 0) {
                manager.await<Any>(any())
            }
        }
    }

    @Test
    fun `Dialogue on any entity`() = runBlocking {
        every { io.sendChat(any()) } returns true
        val npc: NPC = mockk()
        manager.start {
            npc dialogue "Text"
        }

        withContext(Contexts.Game) {
            assertEquals("chat", manager.currentType())
            verify {
                io.sendChat(any())
            }
        }
    }

    @Test
    fun `Send builder statement`() = runBlocking {
        every { io.sendStatement(any()) } returns true
        val npc: NPC = mockk()
        val builder = DialogueBuilder(npc)
        manager.start {
            builder statement "text"
        }
        withContext(Contexts.Game) {
            assertEquals("text", builder.text)
            assertEquals("statement", manager.currentType())
            verify {
                io.sendStatement(builder)
            }
        }
    }

    @Test
    fun `Statement on any entity`() = runBlocking {
        every { io.sendStatement(any()) } returns true
        val npc: NPC = mockk()
        manager.start {
            npc statement "Text"
        }

        withContext(Contexts.Game) {
            assertEquals("statement", manager.currentType())
            verify {
                io.sendStatement(any())
            }
        }
    }

    @Test
    fun `Don't await statement if failed to send`() = runBlocking {
        val npc: NPC = mockk()
        val builder = DialogueBuilder(npc)
        every { io.sendStatement(builder) } returns false
        coEvery { manager.await<Any>(any()) } returns mockk()
        manager.start {
            npc statement "text"
        }
        withContext(Contexts.Game) {
            coVerify(exactly = 0) {
                manager.await<Any>(any())
            }
        }
    }

    @Test
    fun `Send builder choice`() = runBlocking {
        every { io.sendChoice(any()) } returns true
        val npc: NPC = mockk()
        val builder = DialogueBuilder(npc)
        manager.start {
            builder choice "text"
        }
        withContext(Contexts.Game) {
            assertEquals("text", builder.text)
            verify {
                io.sendChoice(builder)
            }
        }
    }

    @Test
    fun `Choice on any entity`() = runBlocking {
        every { io.sendChoice(any()) } returns true
        coEvery { manager.await<Int>("choice") } returns 2
        val npc: NPC = mockk()
        manager.start {
            val choice = npc choice "Text"
            assertEquals(2, choice)
        }

        withContext(Contexts.Game) {
            coVerify {
                io.sendChoice(any())
                manager.await<Int>("choice")
            }
        }
    }

    @Test
    fun `Don't await choice if failed to send`() = runBlocking {
        val npc: NPC = mockk()
        val builder = DialogueBuilder(npc)
        every { io.sendChoice(builder) } returns false
        coEvery { manager.await<Any>(any()) } returns mockk()
        manager.start {
            npc choice "text"
        }
        withContext(Contexts.Game) {
            coVerify(exactly = 0) {
                manager.await<Any>(any())
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
    fun `Animation entity dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = npc animation Expression.Laugh
            assertEquals(npc, builder.target)
            assertEquals(Expression.Laugh, builder.expression)
        }
    }

    @Test
    fun `Animation dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = DialogueBuilder(npc) animation Expression.Laugh
            assertEquals(npc, builder.target)
            assertEquals(Expression.Laugh, builder.expression)
        }
    }

    @Test
    fun `Title entity dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = npc title "text"
            assertEquals(npc, builder.target)
            assertEquals("text", builder.title)
        }
    }

    @Test
    fun `Title dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = DialogueBuilder(npc) title "text"
            assertEquals(npc, builder.target)
            assertEquals("text", builder.title)
        }
    }

    @Test
    fun `Large entity dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = npc large true
            assertEquals(npc, builder.target)
            assertTrue(builder.large)
        }
    }

    @Test
    fun `Large dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = DialogueBuilder(npc) large true
            assertEquals(npc, builder.target)
            assertTrue(builder.large)
        }
    }

    @Test
    fun `Continue entity dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = npc clickToContinue false
            assertEquals(npc, builder.target)
            assertFalse(builder.clickToContinue)
        }
    }

    @Test
    fun `Continue dialogue builder`() {
        val npc: NPC = mockk()
        manager.start {
            val builder: DialogueBuilder = DialogueBuilder(npc) clickToContinue false
            assertEquals(npc, builder.target)
            assertFalse(builder.clickToContinue)
        }
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