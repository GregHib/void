package content.entity.obj.door

import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Yanille house diagonal door
 */
class DiagonalDoorTest : WorldTest() {

    private val closedTile = Tile(2611, 3083)
    private val openedTile = Tile(2611, 3084)

    @Test
    fun `Diagonal door opens 90 degrees`() {
        val player = createPlayer(Tile(2612, 3083))
        closeDoor()
        player.objectOption(GameObjects.find(closedTile) { it.shape == ObjectShape.WALL_DIAGONAL }, "Open")
        tick(3)

        val door = GameObjects.find(openedTile) { it.shape == ObjectShape.WALL_DIAGONAL }
        assertEquals(1, door.rotation)
        assertNull(GameObjects.findOrNull(closedTile) { it.shape == ObjectShape.WALL_DIAGONAL })
        assertEquals(Tile(2612, 3083), player.tile)
    }

    @Test
    fun `Diagonal door closes 90 degrees`() {
        val player = createPlayer(Tile(2610, 3084))
        player.objectOption(GameObjects.find(openedTile, "door_opened"), "Close")
        tick(3)

        val door = GameObjects.find(closedTile) { it.shape == ObjectShape.WALL_DIAGONAL }
        assertEquals(0, door.rotation)
        assertNull(GameObjects.findOrNull(openedTile, "door_opened"))
        assertEquals(Tile(2610, 3084), player.tile)
    }

    @Test
    fun `Player moves out of the way of a diagonal door opening`() {
        val player = createPlayer(openedTile)
        closeDoor()
        player.objectOption(GameObjects.find(closedTile) { it.shape == ObjectShape.WALL_DIAGONAL }, "Open")
        tick(5)

        assertNotNull(GameObjects.findOrNull(openedTile) { it.shape == ObjectShape.WALL_DIAGONAL })
        assertEquals(Tile(2610, 3084), player.tile)
    }

    @Test
    fun `Player moves out of the way of a diagonal door closing`() {
        val player = createPlayer(closedTile)
        player.objectOption(GameObjects.find(openedTile, "door_opened"), "Close")
        tick(5)

        assertNotNull(GameObjects.findOrNull(closedTile) { it.shape == ObjectShape.WALL_DIAGONAL })
        assertEquals(Tile(2612, 3082), player.tile)
    }

    private fun closeDoor() {
        val door = GameObjects.find(openedTile, "door_opened")
        Door.closeDoor(createPlayer(Tile(2611, 3085)), door, ticks = 100)
    }
}
