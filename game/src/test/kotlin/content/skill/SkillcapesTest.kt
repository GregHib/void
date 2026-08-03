package content.skill

import WorldTest
import content.entity.npc.shop.shop
import dialogueContinue
import dialogueOption
import interfaceOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

internal class SkillcapesTest : WorldTest() {

    @Test
    fun `Buying a thieving cape from Martin Thwait grants a free hood`() {
        val player = createPlayer(emptyTile, "thief")
        player.experience.set(Skill.Thieving, 14_000_000.0)
        player.inventory.add("coins", 99_000)
        val martin = createNPC("martin_thwait", emptyTile.addY(1))

        player.npcOption(martin, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption(1)
        tick(4)
        assertEquals("martin_thwaits_lost_and_found_skillcape", player.shop())

        player.interfaceOption("shop", "stock", "Buy-1", item = Item("thieving_cape"), slot = 11 * 6)
        tick()

        assertEquals(1, player.inventory.count("thieving_cape"))
        assertEquals(1, player.inventory.count("thieving_hood"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Buying a trimmed fletching cape from Hickton grants a free hood`() {
        val player = createPlayer(emptyTile, "fletcher")
        player.experience.set(Skill.Fletching, 14_000_000.0)
        player.experience.set(Skill.Cooking, 14_000_000.0)
        player.inventory.add("coins", 99_000)
        val hickton = createNPC("hickton", emptyTile.addY(1))

        player.npcOption(hickton, "Trade")
        tick(4)
        assertEquals("hicktons_archery_emporium_trimmed", player.shop())

        player.interfaceOption("shop", "stock", "Buy-1", item = Item("fletching_cape_t"), slot = 28 * 6)
        tick()

        assertEquals(1, player.inventory.count("fletching_cape_t"))
        assertEquals(1, player.inventory.count("fletching_hood"))
    }
}
