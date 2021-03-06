package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.world.interact.dialogue.type.levelUp

internal class LevelUpTest : DialogueTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        mockkStatic("world.gregs.voidps.engine.client.variable.VariablesKt")
        every { player.setVar(any(), any<Int>()) } just Runs
    }

    @Test
    fun `Send level up`() {
        manager.start(context) {
            levelUp("Congrats\nLevel", 12)
        }
        runBlocking(Contexts.Game) {
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
    fun `Level up not sent if interface not opened`() {
        every { player.open("level_up_dialog") } returns false
        coEvery { context.await<Unit>(any()) } just Runs
        manager.start(context) {
            levelUp("One\nTwo", 1)
        }

        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Unit>(any())
                interfaces.sendText("level_up_dialog", "line1", "One")
            }
        }
    }
}