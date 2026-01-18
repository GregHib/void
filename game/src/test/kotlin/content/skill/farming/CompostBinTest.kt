package content.skill.farming

import WorldTest
import containsMessage
import dialogueOption
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class CompostBinTest : WorldTest() {

    @Test
    fun `Fill empty compost bin with compostable items`() {
        val player = createPlayer(Tile(3056, 3311))
        player.inventory.add("weeds", 2)
        val bin = GameObjects.find(Tile(3056, 3312), "farming_compost_bin_falador")

        player.itemOnObject(bin, 0)
        tick(3)
        assertTrue(player.inventory.isEmpty())
        assertEquals("compostable_2", player["compost_bin_falador", "empty"])
    }

    @Test
    fun `Fill partially filled compost bin with super compostable items`() {
        val player = createPlayer(Tile(3056, 3311))
        player.inventory.add("pineapple", 3)
        player["compost_bin_falador"] = "supercompostable_13"
        val bin = GameObjects.find(Tile(3056, 3312), "farming_compost_bin_falador")

        player.itemOnObject(bin, 0)
        tick(3)
        assertEquals(1, player.inventory.count("pineapple"))
        assertEquals("supercompostable_15", player["compost_bin_falador", "empty"])
    }

    @Test
    fun `Can't fill compost bin with non-compostable items`() {
        val player = createPlayer(Tile(3056, 3311))
        player.inventory.add("shark", 2)
        val bin = GameObjects.find(Tile(3056, 3312), "farming_compost_bin_falador")

        player.itemOnObject(bin, 0)
        tick(3)
        assertEquals(2, player.inventory.count("shark"))
        assertEquals("empty", player["compost_bin_falador", "empty"])
    }

    @Test
    fun `Closing full compost bin rots items`() {
        val player = createPlayer(Tile(3056, 3311))
        player.inventory.add("weeds")
        player["compost_bin_falador"] = "supercompostable_14"
        val bin = GameObjects.find(Tile(3056, 3312), "farming_compost_bin_falador")

        player.itemOnObject(bin, 0)
        tick(3)
        assertTrue(player.inventory.isEmpty())
        assertEquals("compostable_15", player["compost_bin_falador", "empty"])

        player.objectOption(bin, "Close")
        tick(2)

        assertTrue(player.containsMessage("begun to rot"))
        assertTrue(player.timers.contains("farming_tick"))
    }

    @Test
    fun `Empty compost from completed compost bin`() {
        val player = createPlayer(Tile(3056, 3311))
        player.inventory.add("bucket", 3)
        player["compost_bin_falador"] = "compost_15"
        val bin = GameObjects.find(Tile(3056, 3312), "farming_compost_bin_falador")

        player.objectOption(bin, "Empty")
        tick(10)

        assertEquals(3, player.inventory.count("compost"))
        assertEquals(13.5, player.experience.get(Skill.Farming))
        assertEquals("compost_12", player["compost_bin_falador", "empty"])
    }

    @Test
    fun `Empty super-compost from completed compost bin`() {
        val player = createPlayer(Tile(3056, 3311))
        player.inventory.add("bucket", 3)
        player["compost_bin_falador"] = "supercompost_15"
        val bin = GameObjects.find(Tile(3056, 3312), "farming_compost_bin_falador")

        player.objectOption(bin, "Empty")
        tick(10)

        assertEquals(3, player.inventory.count("supercompost"))
        assertEquals(25.5, player.experience.get(Skill.Farming))
        assertEquals("supercompost_12", player["compost_bin_falador", "empty"])
    }

    @Test
    fun `Take tomatoes from completed compost bin`() {
        val player = createPlayer(Tile(3056, 3311))
        player["compost_bin_falador"] = "rotten_tomatoes_15"
        val bin = GameObjects.find(Tile(3056, 3312), "farming_compost_bin_falador")

        player.objectOption(bin, "Take-tomato")
        tick(10)

        assertEquals(3, player.inventory.count("rotten_tomato"))
        assertEquals(13.5, player.experience.get(Skill.Farming))
        assertEquals("rotten_tomatoes_12", player["compost_bin_falador", "empty"])
    }

    @Test
    fun `Dump contents of compost bin`() {
        val player = createPlayer(Tile(3056, 3311))
        player.inventory.add("spade")
        player["compost_bin_falador"] = "supercompostable_13"
        val bin = GameObjects.find(Tile(3056, 3312), "farming_compost_bin_falador")

        player.itemOnObject(bin, 0)
        tick(1)
        player.dialogueOption("line1")
        tick(1)
        assertEquals("empty", player["compost_bin_falador", "empty"])
    }
}
