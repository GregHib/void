package world.gregs.voidps.engine.entity.obj

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameObjectHashMapTest {

    private lateinit var map: GameObjectHashMap

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
    fun `Deallocate only removes the given zone`() {
        val obj = GameObject(1234, 2947, 3904, 0, 10, 2)
        val neighbour = GameObject(1234, 2955, 3904, 0, 10, 2)
        val value = GameObjects.value(false, obj.intId, obj.shape, obj.rotation)
        map.set(x = 2947, y = 3904, level = 0, layer = ObjectLayer.GROUND, mask = value)
        map.set(x = 2955, y = 3904, level = 0, layer = ObjectLayer.GROUND, mask = value)

        map.deallocateZone(neighbour.tile.zone)

        assertEquals(value, map.get(x = 2947, y = 3904, level = 0, layer = ObjectLayer.GROUND))
        assertEquals(-1, map.get(x = 2955, y = 3904, level = 0, layer = ObjectLayer.GROUND))
    }

    @Test
    fun `Removing a flag from an absent tile doesn't store one`() {
        val obj = GameObject(1234, 2947, 3904, 0, 10, 2)

        map.remove(obj, 1)

        // Storing `flags and mask.inv()` would read back as neither empty nor replaced
        assertEquals(-1, map.get(x = 2947, y = 3904, level = 0, layer = ObjectLayer.GROUND))
    }
}
