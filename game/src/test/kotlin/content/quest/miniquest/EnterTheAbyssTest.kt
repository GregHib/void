package content.quest.miniquest

import FakeRandom
import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class EnterTheAbyssTest : WorldTest() {

    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the mini-quest`() {
        // Talk to the mage of zamorak in the wilderness
        val player = createPlayer(Tile(3107, 3556))
        player["rune_mysteries"] = "completed"
        assertEquals("unstarted", player["enter_the_abyss", "unstarted"])

        var mage = findNpc(player, "mage_of_zamorak_wilderness")
        player.npcOption(mage, 0) // Talk-to
        tick()
        player.dialogueOption("continue")
        assertEquals("started", player["enter_the_abyss", "unstarted"])
        player.dialogueOption("line2")

        // Talk to the mage of zamorak in varrock
        player.tele(3258, 3386)
        tick(1)
        mage = findNpc(player, "mage_of_zamorak_varrock")
        player.npcOption(mage, 0) // Talk-to
        tick()
        player.dialogueContinue(5)
        assertTrue(player["enter_abyss_where_runes", false])
        player.dialogueOption("line1") // Where do you get your runes from
        player.dialogueContinue(8)
        player.dialogueOption("line2") // But I'm a loyal servant
        player.dialogueContinue(8)
        player.dialogueOption("line1") // I did it to steal their secrets
        player.dialogueContinue(3)
        assertTrue(player["enter_abyss_offer", false])
        player.dialogueOption("line1") // Deal
        player.dialogueContinue(13)
        assertTrue(player["enter_abyss_has_orb", false])
        assertEquals("scrying", player["enter_the_abyss", "unstarted"])

        // Take the scrying orb to three saradomin wizards
        player.tele(3253, 3401)
        tick()
        var wizard = findNpc(player, "aubury")
        player.npcOption(wizard, 3) // Teleport
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int): Int = when (from) {
                2884 -> 2911
                4807 -> 4832
                else -> from
            }
        })
        tick(4)
        assertTrue(player["scrying_orb_aubury", false])

        player.tele(2681, 3324)
        tick()
        wizard = findNpc(player, "wizard_cromperty")
        player.npcOption(wizard, 2) // Teleport
        tick(4)
        assertTrue(player["scrying_orb_wizard_cromperty", false])

        player.tele(2409, 9815)
        tick()
        wizard = findNpc(player, "brimstail")
        player.npcOption(wizard, 2) // Teleport
        tick(4)
        assertTrue(player["scrying_orb_brimstail", false])
        assertTrue(player.inventory.contains("scrying_orb_full"))

        // Return the scrying orb to the mage of zamorak
        player.tele(3258, 3386)
        tick(1)
        player.npcOption(mage, 0) // Talk-to
        tick()
        player.dialogueContinue(2)
        assertEquals("orb_inspect", player["enter_the_abyss", "unstarted"])
        player.dialogueContinue(3)
        assertTrue(player["enter_abyss_taken_orb", false])
        player.dialogueContinue(11)
        assertEquals("completed", player["enter_the_abyss", "unstarted"])
        assertEquals(1000.0, player.experience.get(Skill.Runecrafting))
    }

    private fun findNpc(player: Player, id: String) = NPCs.at(player.tile.region.toLevel(player.tile.level)).first { it.id == id }
}
