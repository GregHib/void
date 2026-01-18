package content.area.kandarin.ourania

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class OuraniaAltarTest : WorldTest() {

    @Test
    fun `Craft random runes with pure essence`() {
        val player = createPlayer(Tile(3315, 4813))
        player.inventory.add("pure_essence", 28)

        val altar = GameObjects.find(Tile(3315, 4810), "ourania_altar")
        player.objectOption(altar, "Craft-rune")
        tick(1)

        assertFalse(player.inventory.contains("pure_essence"))
        assertEquals(28, player.inventory.count("mind_rune"))
        assertEquals(308.0, player.experience.get(Skill.Runecrafting))
    }

    @Test
    fun `Craft random runes with ardougne medium diary`() {
        val player = createPlayer(Tile(3315, 4813))
        player.inventory.add("pure_essence", 28)
        player["ardougne_medium_diary_complete"] = true

        val altar = GameObjects.find(Tile(3315, 4810), "ourania_altar")
        player.objectOption(altar, "Craft-rune")
        tick(1)

        assertFalse(player.inventory.contains("pure_essence"))
        assertEquals(56, player.inventory.count("mind_rune"))
        assertEquals(308.0, player.experience.get(Skill.Runecrafting))
    }
}
