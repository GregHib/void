package content.skill.farming

import WorldTest
import containsMessage
import itemOnObject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class PlantPotsTest : WorldTest() {
    @Test
    fun `Can't fill plant pot on weeds`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("plant_pot_empty")
        player.inventory.add("gardening_trowel")
        player["farming_veg_patch_falador_se"] = "weeds_2"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(3)

        assertTrue(player.containsMessage("This patch needs weeding"))
        assertEquals(0, player.inventory.count("plant_pot"))
        assertEquals(1, player.inventory.count("plant_pot_empty"))
        assertEquals(0.0, player.experience.get(Skill.Farming))
    }

    @Test
    fun `Fill plant pot on patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("plant_pot_empty")
        player.inventory.add("gardening_trowel")
        player["farming_veg_patch_falador_se"] = "weeds_0"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(3)

        assertEquals(1, player.inventory.count("plant_pot"))
        assertEquals(0, player.inventory.count("plant_pot_empty"))
        assertEquals(0.0, player.experience.get(Skill.Farming))
    }

    @Test
    fun `Fill plant pot requires gardening trowel`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("plant_pot_empty")
        player["farming_veg_patch_falador_se"] = "weeds_0"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(3)

        assertTrue(player.containsMessage("You need a gardening trowel"))
        assertEquals(0, player.inventory.count("plant_pot"))
        assertEquals(1, player.inventory.count("plant_pot_empty"))
        assertEquals(0.0, player.experience.get(Skill.Farming))
    }
}
