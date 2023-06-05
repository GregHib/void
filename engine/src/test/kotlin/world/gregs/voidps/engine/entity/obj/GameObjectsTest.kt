package world.gregs.voidps.engine.entity.obj

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.map.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameObjectsTest {

    private lateinit var objects: GameObjects

    @BeforeEach
    fun setup() {
        objects = GameObjects(mockk(relaxed = true), mockk(relaxed = true))
        GameObjects.LOAD_UNUSED = true
    }

    @Test
    fun `Set an object to a tile`() {
        val obj = GameObject(id = 1234, x = 10, y = 10, plane = 0, type = 10, rotation = 1)

        objects.set(obj.intId, obj.x, obj.y, obj.plane, obj.type, obj.rotation, ObjectDefinition.EMPTY)

        assertEquals(obj, objects[obj.tile, ObjectGroup.INTERACTIVE])
        assertNull(objects[obj.tile, ObjectGroup.WALL])
        assertNull(objects[Tile(10, 10, 1), ObjectGroup.INTERACTIVE])
        objects.clear()
        assertNull(objects[obj.tile, ObjectGroup.INTERACTIVE])
    }

    @Test
    fun `Temporarily remove an object`() {
        val obj = GameObject(id = 1234, x = 10, y = 10, plane = 0, type = 10, rotation = 1)
        objects.set(obj.intId, obj.x, obj.y, obj.plane, obj.type, obj.rotation, ObjectDefinition.EMPTY)

        objects.remove(obj)
        assertNull(objects[obj.tile, ObjectGroup.INTERACTIVE])

        objects.add(obj)
        assertEquals(obj, objects[obj.tile, ObjectGroup.INTERACTIVE])
    }

    @Test
    fun `Temporarily add an object`() {
        val obj = GameObject(id = 1234, x = 10, y = 10, plane = 0, type = 10, rotation = 1)

        objects.add(obj)
        assertEquals(obj, objects[obj.tile, ObjectGroup.INTERACTIVE])
        assertTrue(objects.contains(obj))

        objects.clear(obj.tile.chunk)
        assertNull(objects[obj.tile, ObjectGroup.INTERACTIVE])
        assertFalse(objects.contains(obj))
    }

    @Test
    fun `Add and remove a temp object over an original`() {
        val original = GameObject(123, 10, 10, 0, 10, 1)
        objects.set(original.intId, original.x, original.y, original.plane, original.type, original.rotation, ObjectDefinition.EMPTY)

        val obj = GameObject(id = 1234, x = 10, y = 10, plane = 0, type = 10, rotation = 0)
        objects.add(obj)
        assertEquals(obj, objects[obj.tile, ObjectGroup.INTERACTIVE])

        objects.remove(obj)
        assertEquals(original, objects[obj.tile, ObjectGroup.INTERACTIVE])
    }
}