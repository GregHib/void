package content.skill.firemaking

import WorldTest
import containsMessage
import content.entity.obj.door.Door
import itemOnItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

internal class FiremakingTest : WorldTest() {

    @Test
    fun `Making a fire removes logs and moves player`() {
        val start = emptyTile
        val player = createPlayer(start)
        player.levels.set(Skill.Firemaking, 100)
        player.inventory.add("tinderbox")
        player.inventory.add("logs", 27)

        player.itemOnItem(0, 2)
        tick(5)

        assertTrue(player.inventory.count("logs") < 27)
        assertTrue(player.inventory[1].isNotEmpty())
        assertTrue(player.inventory[2].isEmpty())
        assertEquals(start.add(Direction.WEST), player.tile)
        assertTrue(player.experience.get(Skill.Firemaking) > 0)
    }

    @Test
    fun `Extinguishing swaps lit candles and torches for their unlit form`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("white_candle_lit")
        player.inventory.add("lit_torch")
        player.inventory.add("candle_lantern_lit_white")

        Light.extinguish(player)

        assertEquals("white_candle", player.inventory[0].id)
        assertEquals("unlit_torch", player.inventory[1].id)
        // Lanterns are deliberately left burning.
        assertEquals("candle_lantern_lit_white", player.inventory[2].id)
    }

    @Test
    fun `Can't light a fire in a closed doorway`() {
        val player = firemaker(Tile(3226, 3214))
        assertNull(GameObjects.getLayer(player.tile, ObjectLayer.GROUND_DECORATION))

        player.itemOnItem(0, 1)
        tick(5)

        assertTrue(player.containsMessage("You can't light a fire here."))
        assertNull(GameObjects.getLayer(player.tile, ObjectLayer.GROUND))
        assertEquals(1, player.inventory.count("logs"))
        assertTrue(FloorItems.at(player.tile).none { it.id == "logs" })
    }

    @Test
    fun `Can light a fire where an open door used to be`() {
        val player = firemaker(Tile(3226, 3214))
        Door.openDoor(player, GameObjects.find(Tile(3226, 3214), "door_627_closed"))

        player.itemOnItem(0, 1)
        tick(5)

        assertFalse(player.containsMessage("You can't light a fire here."))
        assertEquals(0, player.inventory.count("logs"))
    }

    @Test
    fun `Can't light a fire under an open door`() {
        val player = firemaker(Tile(3227, 3214))
        Door.openDoor(player, GameObjects.find(Tile(3226, 3214), "door_627_closed"))

        player.itemOnItem(0, 1)
        tick(5)

        assertTrue(player.containsMessage("You can't light a fire here."))
        assertNull(GameObjects.getLayer(player.tile, ObjectLayer.GROUND))
        assertEquals(1, player.inventory.count("logs"))
        assertTrue(FloorItems.at(player.tile).none { it.id == "logs" })
    }

    private fun firemaker(tile: Tile): Player {
        val player = createPlayer(tile)
        player.levels.set(Skill.Firemaking, 100)
        player.inventory.add("tinderbox")
        player.inventory.add("logs")
        return player
    }
}
