package world.gregs.voidps.engine.entity.obj

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.map.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameObjectsTest {

    private lateinit var objects: GameObjects

    @BeforeEach
    fun setup() {
        objects = GameObjects(mockk(relaxed = true), mockk(relaxed = true))
        GameObjects.LOAD_UNUSED = true
    }

    @Test
    fun `Set an object to a tile`() {
        val obj = GameMapObject(1234, 10, 1)
        val tile = Tile(10, 10)

        objects.set(tile.x, tile.y, tile.plane, obj.intId, obj.type, obj.rotation, ObjectDefinition.EMPTY)

        assertEquals(obj, objects.get(tile, ObjectGroup.INTERACTIVE_OBJECT))
        assertNull(objects.get(tile, ObjectGroup.WALL))
        assertNull(objects.get(Tile(10, 9), ObjectGroup.INTERACTIVE_OBJECT))
        objects.clear()
        assertNull(objects.get(tile, ObjectGroup.INTERACTIVE_OBJECT))
    }

    @Test
    fun `Temporarily remove an object`() {
        val obj = GameMapObject(1234, 10, 1)
        val tile = Tile(10, 10)
        objects.set(tile.x, tile.y, tile.plane, obj.intId, obj.type, obj.rotation, ObjectDefinition.EMPTY)

        objects.remove(tile, obj)
        assertNull(objects.get(tile, ObjectGroup.INTERACTIVE_OBJECT))

        objects.add(tile, obj)
        assertEquals(obj, objects.get(tile, ObjectGroup.INTERACTIVE_OBJECT))
    }

    @Test
    fun `Temporarily add an object`() {
        val obj = GameMapObject(1234, 10, 1)
        val tile = Tile(10, 10)

        objects.add(tile, obj)
        assertEquals(obj, objects.get(tile, ObjectGroup.INTERACTIVE_OBJECT))

        objects.remove(tile, obj)
        assertNull(objects.get(tile, ObjectGroup.INTERACTIVE_OBJECT))
    }

    @Test
    fun `Add and remove a temp object over an original`() {
        val original = GameMapObject(123, 10, 1)
        val tile = Tile(10, 10)
        objects.set(tile.x, tile.y, tile.plane, original.intId, original.type, original.rotation, ObjectDefinition.EMPTY)

        val obj = GameMapObject(1234, 10, 0)
        objects.add(tile, obj)
        assertEquals(obj, objects.get(tile, ObjectGroup.INTERACTIVE_OBJECT))

        objects.remove(tile, obj)
        assertEquals(original, objects.get(tile, ObjectGroup.INTERACTIVE_OBJECT))
    }
}