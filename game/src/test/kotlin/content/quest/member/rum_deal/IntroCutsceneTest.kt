package content.quest.member.rum_deal

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Pirate Pete's intro knocks the player out and drops them in the brewery, where Captain
 * Braindeath's conversation should pick up on its own.
 */
class IntroCutsceneTest : WorldTest() {

    override var loadNpcs: Boolean = true

    @Test
    fun `The intro cutscene hands over to Captain Braindeath`() {
        val player = createPlayer(Tile(3672, 3538))
        player["zogre_flesh_eaters"] = "completed"
        player.experience.set(Skill.Slayer, Level.experience(42))
        player["rum_deal"] = "slay_barrelor"
        val pete = createNPC("pirate_pete", Tile(3673, 3538))
        tick(2)

        player.npcOption(pete, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption("line1") // I've decided to help you for free.

        // Ride out the knockout, the cutscene and the hand-over
        var steps = 0
        while (steps < 200) {
            tick()
            steps++
            if (player.dialogue != null) {
                player.dialogueContinue()
            }
            if (player["rum_deal", ""] != "slay_barrelor") {
                break
            }
        }

        assertEquals(Tile(2144, 5108, 1), player.tile, "the player should wake in the brewery")
        assertNotNull(player.dialogue, "Braindeath's conversation should start on its own")
    }
}
