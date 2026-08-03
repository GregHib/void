package content.area.asgarnia.falador

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertTrue

internal class GadrinTest : WorldTest() {

    @Test
    fun `Talk-to opens the skillcape dialogue`() {
        val player = createPlayer(emptyTile, "curious")
        val gadrin = createNPC("gadrin", emptyTile.addY(1))

        player.npcOption(gadrin, "Talk-to")
        tick()

        assertTrue(player.dialogue != null, "the skillcape dialogue opens")
    }

    @Test
    fun `Buy a skillcape of mining with a free hood`() {
        val player = createPlayer(emptyTile, "miner")
        player.experience.set(Skill.Mining, 14_000_000.0)
        player.inventory.add("coins", 99_000)
        val gadrin = createNPC("gadrin", emptyTile.addY(1))

        player.npcOption(gadrin, "Talk-to")
        tick()
        player.dialogueContinue(4)
        player.dialogueOption(1)
        player.dialogueContinue(2)
        player.dialogueOption(2)
        player.dialogueContinue(2)

        assertEquals(1, player.inventory.count("mining_cape"))
        assertEquals(1, player.inventory.count("mining_hood"))
        assertEquals(0, player.inventory.count("coins"))
    }
}
