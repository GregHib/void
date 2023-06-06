package world.gregs.voidps.engine.entity.obj

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.map.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameObjectsTest {

    private lateinit var objects: GameObjects

    @BeforeEach
    fun setup() {
        // Using collision = false to avoid koin [GameObject#def]
        val definitions = mockk<ObjectDefinitions>(relaxed = true)
        every { definitions.get("test") } returns ObjectDefinition(123)
        every { definitions.get("test2") } returns ObjectDefinition(456)
        objects = GameObjects(mockk(relaxed = true), mockk(relaxed = true), definitions)
        GameObjects.LOAD_UNUSED = true
    }

    @Test
    fun `Set an object to a tile`() {
        val obj = GameObject(id = 1234, x = 10, y = 10, plane = 0, type = ObjectType.INTERACTIVE, rotation = 1)

        objects.set(obj.intId, obj.x, obj.y, obj.plane, obj.type, obj.rotation, ObjectDefinition.EMPTY)

        assertEquals(obj, objects.getGroup(obj.tile, ObjectGroup.INTERACTIVE))
        assertNull(objects.getGroup(obj.tile, ObjectGroup.WALL))
        assertNull(objects.getGroup(Tile(10, 10, 1), ObjectGroup.INTERACTIVE))
        objects.clear()
        assertNull(objects.getGroup(obj.tile, ObjectGroup.INTERACTIVE))
    }

    @Test
    fun `Temporarily remove an object`() {
        val obj = GameObject(id = 1234, x = 10, y = 10, plane = 0, type = ObjectType.INTERACTIVE, rotation = 1)
        objects.set(obj.intId, obj.x, obj.y, obj.plane, obj.type, obj.rotation, ObjectDefinition.EMPTY)

        objects.remove(obj)
        assertNull(objects.getGroup(obj.tile, ObjectGroup.INTERACTIVE))

        objects.add(obj)
        assertEquals(obj, objects.getGroup(obj.tile, ObjectGroup.INTERACTIVE))
    }

    @Test
    fun `Temporarily add an object`() {
        val obj = GameObject(id = 1234, x = 10, y = 10, plane = 0, type = ObjectType.INTERACTIVE, rotation = 1)

        objects.add(obj)
        assertEquals(obj, objects.getGroup(obj.tile, ObjectGroup.INTERACTIVE))
        assertTrue(objects.contains(obj))

        objects.reset(obj.tile.chunk)
        assertNull(objects.getGroup(obj.tile, ObjectGroup.INTERACTIVE))
        assertFalse(objects.contains(obj))
    }

    @Test
    fun `Add and remove a temp object over an original`() {
        val original = GameObject(123, 10, 10, 0, 10, 1)
        objects.set(original.intId, original.x, original.y, original.plane, original.type, original.rotation, ObjectDefinition.EMPTY)

        val obj = GameObject(id = 1234, x = 10, y = 10, plane = 0, type = ObjectType.INTERACTIVE, rotation = 0)
        objects.add(obj)
        assertEquals(obj, objects.getGroup(obj.tile, ObjectGroup.INTERACTIVE))

        objects.remove(obj)
        assertEquals(original, objects.getGroup(obj.tile, ObjectGroup.INTERACTIVE))
    }

    @Test
    fun `Temporary object is removed after ticks`() {
        val obj = objects.add(id = "test", tile = Tile(100, 100), type = ObjectType.INTERACTIVE, rotation = 2, ticks = 5, collision = false)
        repeat(5) {
            assertTrue(objects.contains(obj))
            objects.timers.run()
        }
        assertFalse(objects.contains(obj))
    }

    @Test
    fun `Removed object is returned after ticks`() {
        val obj = GameObject(id = 123, x = 100, y = 100, plane = 0, type = ObjectType.INTERACTIVE, rotation = 2)
        objects.add(obj, collision = false)
        objects.remove(obj = obj, ticks = 5, collision = false)
        repeat(5) {
            assertFalse(objects.contains(obj))
            objects.timers.run()
        }
        assertTrue(objects.contains(obj))
    }

    @Test
    fun `Replaced temporary object is undone after ticks`() {
        val obj = GameObject(id = 5678, x = 100, y = 100, plane = 0, type = ObjectType.INTERACTIVE, rotation = 2)
        objects.add(obj, collision = false)
        val replacement = objects.replace(obj, "test", Tile(101, 100), ObjectType.INTERACTIVE, 1, ticks = 5, collision = false)
        repeat(5) {
            assertFalse(objects.contains(obj))
            assertTrue(objects.contains(replacement))
            objects.timers.run()
        }
        assertTrue(objects.contains(obj))
        assertFalse(objects.contains(replacement))
    }

    @Test
    fun `Replaced original object is undone after ticks`() {
        val original = GameObject(id = 5678, x = 100, y = 100, plane = 0, type = ObjectType.INTERACTIVE, rotation = 2)
        objects.set(original.intId, original.x, original.y, original.plane, original.type, original.rotation, ObjectDefinition.EMPTY)
        val replacement = objects.replace(original, "test", Tile(101, 100), ObjectType.INTERACTIVE, 1, ticks = 5, collision = false)
        repeat(5) {
            assertFalse(objects.contains(original))
            assertTrue(objects.contains(replacement))
            objects.timers.run()
        }
        assertTrue(objects.contains(original))
        assertFalse(objects.contains(replacement))
    }
}