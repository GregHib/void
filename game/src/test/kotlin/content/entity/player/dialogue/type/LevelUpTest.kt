package content.entity.player.dialogue.type

import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.suspend.Suspension
import kotlin.test.assertTrue

internal class LevelUpTest : DialogueTest() {

    @Test
    fun `Send level up`() {
        var resumed = false
        dialogue {
            levelUp(Skill.Runecrafting, "Congrats\nLevel")
            resumed = true
        }
        (player.suspension as Suspension.Continue).resume()
        verify {
            player.open("dialogue_level_up")
            interfaces.sendText("dialogue_level_up", "line1", "Congrats")
            interfaces.sendText("dialogue_level_up", "line2", "Level")
            player["level_up_icon"] = Skill.Runecrafting.name
        }
        assertTrue(resumed)
    }

    @Test
    fun `Level up not sent if interface not opened`() {
        every { player.open("dialogue_level_up") } returns false
        dialogueBlocking {
            levelUp(Skill.Agility, "One\nTwo")
        }

        coVerify(exactly = 0) {
            interfaces.sendText("dialogue_level_up", "line1", "One")
        }
    }
}
