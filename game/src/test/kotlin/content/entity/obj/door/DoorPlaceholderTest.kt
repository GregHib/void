package content.entity.obj.door

import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import walk
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DoorPlaceholderTest : WorldTest() {

    @Test
    fun `Opening a door leaves an invisible wall behind which doesn't block the doorway`() {
        val player = createPlayer(Tile(3227, 3214))
        val door = GameObjects.find(Tile(3226, 3214), "door_627_closed")

        player.objectOption(door, "Open")
        tick(2)

        val opened = GameObjects.getLayer(Tile(3227, 3214), ObjectLayer.WALL)
        assertNotNull(opened)
        assertTrue(opened.id.endsWith("_opened"))
        val placeholder = GameObjects.findLayerOrNull(Tile(3226, 3214), ObjectLayer.WALL, "inviswall")
        assertNotNull(placeholder)
        assertEquals(ObjectShape.WALL_STRAIGHT, placeholder.shape)

        player.walk(Tile(3225, 3214))
        tick(3)
        assertEquals(Tile(3225, 3214), player.tile)

        player.objectOption(opened, "Close")
        tick(2)

        assertNotNull(GameObjects.findLayerOrNull(Tile(3226, 3214), ObjectLayer.WALL, "door_627_closed"))
        assertNull(GameObjects.getLayer(Tile(3227, 3214), ObjectLayer.WALL))
    }

    @Test
    fun `Opening double doors leaves invisible walls behind both`() {
        val player = createPlayer(Tile(3212, 3221))
        val door = GameObjects.find(Tile(3213, 3221), "large_door_39_closed")

        player.objectOption(door, "Open")
        tick(2)

        assertNotNull(GameObjects.findLayerOrNull(Tile(3213, 3221), ObjectLayer.WALL, "inviswall"))
        assertNotNull(GameObjects.findLayerOrNull(Tile(3213, 3222), ObjectLayer.WALL, "inviswall"))

        player.walk(Tile(3214, 3221))
        tick(3)
        assertEquals(Tile(3214, 3221), player.tile)

        GameObjects.timers.reset()

        assertNotNull(GameObjects.findLayerOrNull(Tile(3213, 3221), ObjectLayer.WALL, "large_door_39_closed"))
        assertNotNull(GameObjects.findLayerOrNull(Tile(3213, 3222), ObjectLayer.WALL, "large_door_40_closed"))
    }

    @Test
    fun `Walking through a door without collision leaves an invisible centrepiece behind`() {
        val player = createPlayer(Tile(3227, 3214))
        val door = GameObjects.find(Tile(3226, 3214), "door_627_closed")

        Door.openDoor(player, door, ticks = 3, collision = false)

        val placeholder = GameObjects.findLayerOrNull(Tile(3226, 3214), ObjectLayer.GROUND, "inviswall")
        assertNotNull(placeholder)
        assertEquals(ObjectShape.CENTRE_PIECE_STRAIGHT, placeholder.shape)

        tick(3)

        assertNull(GameObjects.getLayer(Tile(3226, 3214), ObjectLayer.GROUND))
        assertNotNull(GameObjects.findLayerOrNull(Tile(3226, 3214), ObjectLayer.WALL, "door_627_closed"))
    }
}
