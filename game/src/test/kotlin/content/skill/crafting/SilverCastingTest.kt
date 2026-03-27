package content.skill.crafting

import WorldTest
import itemOnObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class SilverCastingTest : WorldTest() {

    @Test
    fun `Silver casting`() {
        val player = createPlayer(Tile(3227, 3255))
        player.levels.set(Skill.Crafting, 99)
        player.inventory.add("bolt_mould")
        player.inventory.add("silver_bar")

        val furnace = GameObjects.find(Tile(3226, 3256), "furnace_lumbridge")

        player.itemOnObject(furnace, 0)
        tick(4)

        assertEquals(1, player.inventory.count("bolt_mould"))
        assertEquals(0, player.inventory.count("silver_bar"))
        assertEquals(10, player.inventory.count("silver_bolts_unf"))
        assertNotEquals(0.0, player.experience.get(Skill.Crafting))
    }
}
