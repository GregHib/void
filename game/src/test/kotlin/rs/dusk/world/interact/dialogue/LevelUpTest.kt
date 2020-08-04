package rs.dusk.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.client.ui.Interfaces
import rs.dusk.engine.client.ui.dialogue.DialogueIO
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.player.Player

internal class LevelUpTest {

    lateinit var interfaces: Interfaces
    lateinit var manager: Dialogues
    lateinit var io: DialogueIO
    lateinit var player: Player

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.client.ui.InterfacesKt")
        mockkStatic("rs.dusk.engine.client.variable.VariablesKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        io = mockk(relaxed = true)
        manager = spyk(Dialogues(io, player))
        every { player.open(any()) } returns true
        every { player.setVar(any(), any<Int>()) } just Runs
        every { player.interfaces } returns interfaces
    }

    @Test
    fun `Send level up`() = runBlocking {
        manager.start {
            levelUp("Congrats\nLevel", 12)
        }
        withContext(Contexts.Game) {
            assertEquals("level", manager.currentType())
            verify {
                player.open("level_up_dialog")
                interfaces.sendText("level_up_dialog", "line1", "Congrats")
                interfaces.sendText("level_up_dialog", "line2", "Level")
                player.setVar("level_up_icon", 12)
            }
        }
    }

    @Test
    fun `Level up not sent if interface not opened`() = runBlocking {
        every { player.open("level_up_dialog") } returns false
        coEvery { manager.await<Unit>(any()) } just Runs
        manager.start {
            levelUp("One\nTwo", 1)
        }

        withContext(Contexts.Game) {
            coVerify(exactly = 0) {
                manager.await<Unit>(any())
                interfaces.sendText("level_up_dialog", "line1", "One")
            }
        }
    }
}