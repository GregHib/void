package content.skill.farming

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class FarmingPatchTest : WorldTest() {
    @Test
    fun `Check farming patch guide`() {
        val player = createPlayer(Tile(3057, 3308))
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_2"]!!

        player.objectOption(patch, "Guide")

        tick()

        assertTrue(player.interfaces.contains("skill_guide"))
    }

    @Test
    fun `Inspect farming patch`() {
        val player = createPlayer(Tile(3057, 3308))
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_2"]!!

        player.objectOption(patch, "Inspect")

        tick()

        assertTrue(player.containsMessage("This is an allotment."))
    }

    @Test
    fun `Rake farming patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("rake")
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_2"]!!

        player.objectOption(patch, "Rake")

        tick(10)

        assertEquals(3, player.inventory.count("weeds"))
        assertEquals(24.0, player.experience.get(Skill.Farming))
        assertEquals("weeds_0", player["patch_falador_se_allotment", "empty"])
    }

}