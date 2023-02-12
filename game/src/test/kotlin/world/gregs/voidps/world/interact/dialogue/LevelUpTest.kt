package world.gregs.voidps.world.interact.dialogue

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.suspend.ContinueSuspension
import world.gregs.voidps.world.interact.dialogue.type.levelUp
import kotlin.test.assertTrue

internal class LevelUpTest : DialogueTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        mockkStatic("world.gregs.voidps.engine.client.variable.VariablesKt")
        every { player.setVar(any(), any<Int>()) } just Runs
    }

    @Test
    fun `Send level up`() {
        var resumed = false
        dialogue {
            levelUp("Congrats\nLevel", Skill.Runecrafting)
            resumed = true
        }
        (player.suspension as ContinueSuspension).resume()
        verify {
            player.open("dialogue_level_up")
            interfaces.sendText("dialogue_level_up", "line1", "Congrats")
            interfaces.sendText("dialogue_level_up", "line2", "Level")
            player.setVar("level_up_icon", Skill.Runecrafting.name)
        }
        assertTrue(resumed)
    }

    @Test
    fun `Level up not sent if interface not opened`() {
        every { player.open("dialogue_level_up") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                levelUp("One\nTwo", Skill.Agility)
            }
        }

        coVerify(exactly = 0) {
            interfaces.sendText("dialogue_level_up", "line1", "One")
        }
    }
}
