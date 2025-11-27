package content.skill.farming

import WorldTest
import containsMessage
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class FarmingPatchTest : WorldTest() {
    @Test
    fun `Check farming patch guide`() {
        val player = createPlayer(Tile(3057, 3308))
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.objectOption(patch, "Guide")
        tick()

        assertTrue(player.interfaces.contains("skill_guide"))
    }

    @Test
    fun `Inspect farming patch`() {
        val player = createPlayer(Tile(3057, 3308))
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.objectOption(patch, "Inspect")
        tick()

        assertTrue(player.containsMessage("This is an allotment."))
    }

    @Test
    fun `Rake farming patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("rake")
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.objectOption(patch, "Rake")
        tick(10)

        assertEquals(3, player.inventory.count("weeds"))
        assertEquals(24.0, player.experience.get(Skill.Farming))
        assertEquals("weeds_0", player["farming_veg_patch_falador_se", "empty"])
    }

    @Test
    fun `Compost an empty patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("compost")
        // TODO can you compost weeds?
        player["farming_veg_patch_falador_se"] = "weeds_0"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(3)

        assertTrue(player.containsVarbit("patch_compost", "farming_veg_patch_falador_se"))
        assertEquals(1, player.inventory.count("bucket"))
        assertEquals(18.0, player.experience.get(Skill.Farming))
        assertEquals("weeds_0", player["farming_veg_patch_falador_se", "empty"])
    }

    @Test
    fun `Can't compost a composted patch`() {
        // TODO can you super compost a composted patch?
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("compost")
        player["farming_veg_patch_falador_se"] = "weeds_0"
        player.addVarbit("patch_compost", "farming_veg_patch_falador_se")
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(3)

        assertTrue(player.containsMessage("This allotment has already been treated"))
        assertEquals(1, player.inventory.count("compost"))
        assertEquals(0, player.inventory.count("bucket"))
        assertEquals(0.0, player.experience.get(Skill.Farming))
        assertEquals("weeds_0", player["farming_veg_patch_falador_se", "empty"])
    }

    @Test
    fun `Plant potatoes in an empty patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("potato_seed", 3)
        player.inventory.add("seed_dibber")
        player["farming_veg_patch_falador_se"] = "weeds_0"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(4)

        assertEquals(0, player.inventory.count("potato_seed"))
        assertEquals(8.0, player.experience.get(Skill.Farming))
        assertEquals("potato_0", player["farming_veg_patch_falador_se", "empty"])
    }

    @Test
    fun `Can't plant seeds without a dibber`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("potato_seed", 3)
        player["farming_veg_patch_falador_se"] = "weeds_0"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(4)

        assertTrue(player.containsMessage("You need a seed dibber"))
        assertEquals(3, player.inventory.count("potato_seed"))
        assertEquals(0.0, player.experience.get(Skill.Farming))
        assertEquals("weeds_0", player["farming_veg_patch_falador_se", "empty"])
    }

    @Test
    fun `Can't plant seeds in an active patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("potato_seed", 3)
        player["farming_veg_patch_falador_se"] = "potato_0"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(4)

        assertNotNull(player.dialogue)
        assertEquals(3, player.inventory.count("potato_seed"))
        assertEquals(0.0, player.experience.get(Skill.Farming))
        assertEquals("potato_0", player["farming_veg_patch_falador_se", "empty"])
    }

    @Test
    fun `Water crop patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("watering_can_8")
        player["farming_veg_patch_falador_se"] = "potato_0"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(3)

        assertEquals(0, player.inventory.count("watering_can_8"))
        assertEquals(1, player.inventory.count("watering_can_7"))
        assertEquals(0.0, player.experience.get(Skill.Farming))
        assertEquals("potato_watered_0", player["farming_veg_patch_falador_se", "empty"])
    }

    @Test
    fun `Can't water empty patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("watering_can_8")
        player["farming_veg_patch_falador_se"] = "weeds_0"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(3)

        assertTrue(player.containsMessage("This patch doesn't need watering"))
        assertEquals(1, player.inventory.count("watering_can_8"))
        assertEquals(0, player.inventory.count("watering_can_7"))
        assertEquals(0.0, player.experience.get(Skill.Farming))
        assertEquals("weeds_0", player["farming_veg_patch_falador_se", "empty"])
    }

    @Test
    fun `Can't water watered patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("watering_can_8")
        player["farming_veg_patch_falador_se"] = "potato_watered_0"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(3)

        assertTrue(player.containsMessage("This patch doesn't need watering"))
        assertEquals(1, player.inventory.count("watering_can_8"))
        assertEquals(0, player.inventory.count("watering_can_7"))
        assertEquals(0.0, player.experience.get(Skill.Farming))
    }

    @Test
    fun `Clear dead patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.inventory.add("spade")
        player["farming_veg_patch_falador_se"] = "potato_dead_1"
        val patch = objects[Tile(3058, 3308), "farming_veg_patch_falador_se"]!!

        player.itemOnObject(patch, 0)
        tick(3)

        assertEquals("weeds_0", player["farming_veg_patch_falador_se", "empty"])
    }
}
