package world.gregs.voidps.engine.entity.obj

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk

class GameObjectHashMapTest {

    private lateinit var map: GameObjectMap

    @BeforeEach
    fun setup() {
        map = GameObjectHashMap()
    }

    @Test
    fun `Set a default object`() {
        val obj = GameObject(1234, 115, 110, 1, 10, 2)
        val value = GameObjects.value(false, obj.intId, obj.shape, obj.rotation)
        map.set(x = 115, y = 110, level = 1, layer = 10, mask = value)

        val result = map.get(x = 115, y = 110, level = 1, layer = ObjectLayer.GROUND)
        assertEquals(value, result)
    }

    @Test
    fun `Replace an object`() {
        val obj = GameObject(1234, 2500, 3900, 1, 10, 2)
        val value = GameObjects.value(false, obj.intId, obj.shape, obj.rotation)
        map.set(x = 2500, y = 3900, level = 1, layer = 10, mask = value)
        map.add(obj, 1)
        val result = map.get(x = 2500, y = 3900, level = 1, layer = ObjectLayer.GROUND)
        assertEquals(GameObjects.value(true, obj.intId, obj.shape, obj.rotation), result)
    }

    @Test
    fun `Set directly`() {
        val obj = GameObject(1234, 2500, 3900, 1, 10, 2)
        val value = GameObjects.value(false, obj.intId, obj.shape, obj.rotation)
        val tileIndex = Tile.index(obj.x, obj.y, ObjectLayer.GROUND)
        val zoneIndex = Chunk.tileIndex(obj.x, obj.y, obj.plane)
        map[zoneIndex, tileIndex] = value
        val result = map.get(x = 2500, y = 3900, level = 1, layer = ObjectLayer.GROUND)
        assertEquals(value, result)
    }
}