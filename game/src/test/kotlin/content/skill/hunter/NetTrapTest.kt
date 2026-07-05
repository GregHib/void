package content.skill.hunter

import FakeRandom
import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class NetTrapTest : WorldTest() {
    @Test
    fun `Catch a salamander`() {
        val player = createPlayer(Tile(2453, 3220))
        val tree = GameObjects.find(Tile(2453, 3219), "red_net")
        val start = player.tile
        player.inventory.add("small_fishing_net")
        player.inventory.add("rope")
        player.levels.set(Skill.Hunter, 99)

        player.objectOption(tree, "Set-trap")
        tick(2)
        assertTrue(GameObjects.at(start).any { it.id == "net" })
        assertEquals(Tile(2452, 3220), player.tile)
        createNPC("red_salamander", player.tile)

        tick(22)

        val trap = GameObjects.find(tree.tile, "red_net_caught")

        player.objectOption(trap, "Check")
        tick(3)
        assertEquals(1, player.inventory.count("rope"))
        assertEquals(1, player.inventory.count("small_fishing_net"))
        assertEquals(1, player.inventory.count("red_salamander"))
        assertNotEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Can't catch without hunter level`() {
        val player = createPlayer(Tile(3537, 3445))
        val tree = GameObjects.find(Tile(3538, 3445), "swamp_net")
        val start = player.tile
        player.inventory.add("small_fishing_net")
        player.inventory.add("rope")

        player.objectOption(tree, "Set-trap")
        tick(2)
        assertFalse(GameObjects.at(start).any { it.id == "net" })
        assertEquals(start, player.tile)
    }

    @Test
    fun `Fail to catch`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 4) 0 else until - 1
        })
        val player = createPlayer(Tile(3407, 3094))
        val tree = GameObjects.find(Tile(3407, 3093), "orange_net")
        val start = player.tile
        player.inventory.add("small_fishing_net")
        player.inventory.add("rope")
        player.levels.set(Skill.Hunter, 50)

        player.objectOption(tree, "Set-trap")
        tick(2)
        assertTrue(GameObjects.at(start).any { it.id == "net" })
        assertEquals(Tile(3406, 3094), player.tile)
        createNPC("orange_salamander", player.tile)

        tick(24)

        val trap = GameObjects.find(tree.tile, "orange_net")
        assertNotNull(trap)
        assertTrue(FloorItems.at(start).any { it.id == "rope" })
        assertTrue(FloorItems.at(start).any { it.id == "small_fishing_net" })
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }
}
