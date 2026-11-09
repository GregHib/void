package content.area.kandarin.seers_village

import WorldTest
import containsMessage
import content.entity.effect.transform
import itemOnItem
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class ElementalWorkshopTest : WorldTest() {

    @Test
    fun `Cut the battered book to find the key`() {
        val player = createPlayer(Tile(2716, 3482))
        player.inventory.add("knife")
        player.inventory.add("battered_book")

        player.itemOnItem(0, 1)
        tick()

        assertEquals(1, player.inventory.count("slashed_book"))
        assertEquals(1, player.inventory.count("battered_key"))
    }

    @Test
    fun `Open the odd wall with the battered key`() {
        val player = createPlayer(Tile(2710, 3496))
        player.inventory.add("battered_key")
        val wall = GameObjects.find(Tile(2710, 3495), "elemental_workshop_odd_wall")

        player.objectOption(wall, "Open")
        tick(5)

        assertEquals(3495, player.tile.y)
        assertTrue(player.containsMessage("You use the battered key to open the doors."))
    }

    @Test
    fun `Power the furnace and smelt elemental ore`() {
        val player = createPlayer(Tile(2726, 9907))
        player.levels.set(Skill.Smithing, 20)
        player.levels.set(Skill.Crafting, 20)

        val right = GameObjects.find(Tile(2726, 9908), "elemental_workshop_water_controls_right")
        player.objectOption(right, "Turn")
        tick(5)
        player.tele(2713, 9907)
        val left = GameObjects.find(Tile(2713, 9908), "elemental_workshop_water_controls_left")
        player.objectOption(left, "Turn")
        tick(5)
        assertTrue(player["elemental_workshop_left_water_control", false])
        assertTrue(player["elemental_workshop_right_water_control", false])

        player.tele(2721, 9906)
        val waterLever = GameObjects.find(Tile(2722, 9906), "elemental_workshop_water_wheel_lever")
        player.objectOption(waterLever, "Pull")
        tick(3)
        assertTrue(player["elemental_workshop_water_wheel", false])

        player.tele(2734, 9881)
        player.inventory.add("needle")
        player.inventory.add("thread")
        player.inventory.add("leather")
        val bellows = GameObjects.find(Tile(2734, 9882), "elemental_workshop_bellows")
        player.objectOption(bellows, "Fix")
        tick(3)
        assertTrue(player["elemental_workshop_bellows_fixed", false])

        player.tele(2734, 9886)
        val bellowsLever = GameObjects.find(Tile(2734, 9887), "elemental_workshop_bellows_lever")
        player.objectOption(bellowsLever, "Pull")
        tick(3)
        assertTrue(player["elemental_workshop_bellows", false])

        player.inventory.clear()
        player.inventory.add("a_stone_bowl_full")
        player.inventory.add("elemental_ore")
        player.inventory.add("coal", 4)
        player.tele(2723, 9875)
        val furnace = GameObjects.find(Tile(2725, 9874), "elemental_workshop_furnace")
        player.itemOnObject(furnace, 0)
        tick(5)
        assertTrue(player["elemental_workshop_furnace", false])

        player.itemOnObject(furnace, player.inventory.indexOf("elemental_ore"))
        tick(8)
        assertEquals(1, player.inventory.count("elemental_bar"))
        assertEquals(0, player.inventory.count("coal"))
    }

    @Test
    fun `Make an elemental shield at the workbench`() {
        val player = createPlayer(Tile(2718, 9887))
        player.levels.set(Skill.Smithing, 20)
        player.inventory.add("elemental_bar")
        player.inventory.add("slashed_book")
        player.inventory.add("hammer")
        val workbench = GameObjects.find(Tile(2717, 9887), "elemental_workshop_workbench")

        player.itemOnObject(workbench, 0)
        tick(3)

        assertEquals(1, player.inventory.count("elemental_shield"))
    }

    @Test
    fun `Mining an elemental rock wakes an earth elemental`() {
        val player = createPlayer(Tile(2691, 9880))
        player.levels.set(Skill.Mining, 20)
        player.inventory.add("bronze_pickaxe")
        val rock = createNPC("elemental_rock_elemental_workshop", Tile(2690, 9880))

        player.npcOption(rock, "Mine")
        tick(3)

        assertEquals("earth_elemental_2", rock.transform)
    }

    @Test
    fun `Normal furnaces can't smelt elemental ore`() {
        val player = createPlayer(Tile(3227, 3255))
        player.inventory.add("elemental_ore")
        val furnace = GameObjects.find(Tile(3226, 3256), "furnace_lumbridge")

        player.itemOnObject(furnace, 0)
        tick(2)

        assertTrue(player.containsMessage("This furnace is not hot enough to smelt elemental ore."))
    }
}
