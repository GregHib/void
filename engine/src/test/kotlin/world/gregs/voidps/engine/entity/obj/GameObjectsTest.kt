package world.gregs.voidps.engine.entity.obj

import io.mockk.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declare
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectAddition
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectRemoval
import world.gregs.voidps.type.Tile
import java.util.LinkedList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameObjectsTest : KoinMock() {

    private lateinit var objects: GameObjects
    private lateinit var updates: ZoneBatchUpdates
    private lateinit var events: Events
    private lateinit var calls: LinkedList<GameObject>

    @BeforeEach
    fun setup() {
        // Using collision = false to avoid koin [GameObject#def]
        val definitions = mockk<ObjectDefinitions>(relaxed = true)
        every { definitions.get(any<Int>()) } returns ObjectDefinition()
        every { definitions.get("test") } returns ObjectDefinition(123)
        every { definitions.get("test2") } returns ObjectDefinition(456)
        declare { definitions }
        updates = mockk(relaxed = true)
        objects = GameObjects(mockk(relaxed = true), mockk(relaxed = true), updates, definitions, storeUnused = true)
        events = spyk(Events())
        Events.setEvents(events)
        calls = spyk(LinkedList())
        val spawn: (GameObject) -> Unit = {
            calls.add(it)
        }
        Spawn.objectSpawns["*"] = mutableListOf(spawn)
    }

    @Test
    fun `Set an object to a tile`() {
        val obj = GameObject(id = 1234, x = 10, y = 10, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1)

        objects.set(obj.intId, obj.x, obj.y, obj.level, obj.shape, obj.rotation, ObjectDefinition.EMPTY)

        assertEquals(obj, objects.getLayer(obj.tile, ObjectLayer.GROUND))
        assertNull(objects.getLayer(obj.tile, ObjectLayer.WALL))
        assertNull(objects.getLayer(Tile(10, 10, 1), ObjectLayer.GROUND))
        objects.clear()
        assertNull(objects.getLayer(obj.tile, ObjectLayer.GROUND))
        assertEquals(obj, calls.pop())
        verify(exactly = 0) {
            events.emit(obj, any())
        }
    }

    @Test
    fun `Temporarily remove an object`() {
        val obj = GameObject(id = 1234, x = 10, y = 10, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1)
        objects.set(obj.intId, obj.x, obj.y, obj.level, obj.shape, obj.rotation, ObjectDefinition.EMPTY)

        objects.remove(obj)
        assertNull(objects.getLayer(obj.tile, ObjectLayer.GROUND))

        objects.add(obj)
        assertEquals(obj, objects.getLayer(obj.tile, ObjectLayer.GROUND))
        verify {
            updates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
        }
        assertEquals(obj, calls.pop())
        verify(exactly = 0) {
            events.emit(obj, any())
        }
    }

    @Test
    fun `Temporarily add an object`() {
        val obj = GameObject(id = 1234, x = 10, y = 10, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1)

        objects.add(obj)
        assertEquals(obj, objects.getLayer(obj.tile, ObjectLayer.GROUND))
        assertTrue(objects.contains(obj))

        objects.reset(obj.tile.zone)
        assertNull(objects.getLayer(obj.tile, ObjectLayer.GROUND))
        assertFalse(objects.contains(obj))
        verifyOrder {
            updates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 1234, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
            calls.add(obj)
            events.emit(obj, Despawn)
        }
    }

    @Test
    fun `Override temporary object`() {
        val obj = GameObject(id = 1234, x = 10, y = 10, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1)
        val override = GameObject(id = 4321, x = 10, y = 10, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0)

        objects.add(obj)
        assertEquals(obj, objects.getLayer(obj.tile, ObjectLayer.GROUND))
        assertTrue(objects.contains(obj))
        assertFalse(objects.contains(override))
        objects.add(override)
        assertEquals(override, objects.getLayer(obj.tile, ObjectLayer.GROUND))
        assertTrue(objects.contains(override))
        assertFalse(objects.contains(obj))

        objects.remove(override)
        assertNull(objects.getLayer(obj.tile, ObjectLayer.GROUND))
        assertFalse(objects.contains(obj))
        assertFalse(objects.contains(override))
        verifyOrder {
            updates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 1234, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
            calls.add(obj)
            updates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 4321, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            calls.add(override)
            updates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            events.emit(override, Despawn)
        }
    }

    @Test
    fun `Add and remove a temp object over an original`() {
        val original = GameObject(id = 123, x = 10, y = 10, level = 0, shape = 10, rotation = 1)
        objects.set(original.intId, original.x, original.y, original.level, original.shape, original.rotation, ObjectDefinition.EMPTY)

        val obj = GameObject(id = 1234, x = 10, y = 10, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0)
        objects.add(obj)
        assertEquals(obj, objects.getLayer(obj.tile, ObjectLayer.GROUND))

        objects.remove(obj)
        assertEquals(original, objects.getLayer(obj.tile, ObjectLayer.GROUND))

        verifyOrder {
            updates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
            updates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 1234, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            calls.add(obj)
            updates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            events.emit(obj, Despawn)
            updates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 123, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
        }
    }

    @Test
    fun `Override temp over an original object`() {
        val original = GameObject(id = 123, x = 10, y = 10, level = 0, shape = 10, rotation = 1)
        objects.set(original.intId, original.x, original.y, original.level, original.shape, original.rotation, ObjectDefinition.EMPTY)

        val obj = GameObject(id = 1234, x = 10, y = 10, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0)
        objects.add(obj)
        assertEquals(obj, objects.getLayer(obj.tile, ObjectLayer.GROUND))

        val override = GameObject(id = 4321, x = 10, y = 10, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0)
        objects.add(override)
        assertEquals(override, objects.getLayer(override.tile, ObjectLayer.GROUND))

        objects.remove(override)
        assertEquals(original, objects.getLayer(obj.tile, ObjectLayer.GROUND))

        verifyOrder {
            // Add 1234
            updates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
            updates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 1234, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            calls.add(obj)
            // Add 4321
            updates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            events.emit(obj, Despawn)
            updates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 4321, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            calls.add(override)
            // Remove 4321
            updates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            events.emit(override, Despawn)
            updates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 123, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
        }
    }

    @Test
    fun `Temporary object is removed after ticks`() {
        val obj = objects.add(id = "test", tile = Tile(100, 100), shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 2, ticks = 5, collision = false)
        repeat(5) {
            assertTrue(objects.contains(obj))
            objects.timers.run()
        }
        assertFalse(objects.contains(obj))
        verifyOrder {
            calls.add(obj)
            events.emit(obj, Despawn)
        }
    }

    @Test
    fun `Removed object is returned after ticks`() {
        val obj = GameObject(id = 123, x = 100, y = 100, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 2)
        objects.add(obj, collision = false)
        objects.remove(obj = obj, ticks = 5, collision = false)
        repeat(5) {
            assertFalse(objects.contains(obj))
            objects.timers.run()
        }
        assertTrue(objects.contains(obj))
        verifyOrder {
            calls.add(obj)
            events.emit(obj, Despawn)
            calls.add(obj)
        }
    }

    @Test
    fun `Replaced temporary object is undone after ticks`() {
        val obj = GameObject(id = 5678, x = 100, y = 100, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 2)
        objects.add(obj, collision = false)
        val replacement = objects.replace(obj, "test", Tile(101, 100), ObjectShape.CENTRE_PIECE_STRAIGHT, 1, ticks = 5, collision = false)
        repeat(5) {
            assertFalse(objects.contains(obj))
            assertTrue(objects.contains(replacement))
            objects.timers.run()
        }
        assertTrue(objects.contains(obj))
        assertFalse(objects.contains(replacement))
        verifyOrder {
            calls.add(obj)
            events.emit(obj, Despawn)
            calls.add(replacement)
            calls.add(obj)
        }
    }

    @Test
    fun `Replaced original object is undone after ticks`() {
        val original = GameObject(id = 5678, x = 100, y = 100, level = 0, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 2)
        objects.set(original.intId, original.x, original.y, original.level, original.shape, original.rotation, ObjectDefinition.EMPTY)
        val replacement = objects.replace(original, "test", Tile(101, 100), ObjectShape.CENTRE_PIECE_STRAIGHT, 1, ticks = 5, collision = false)
        repeat(5) {
            assertFalse(objects.contains(original))
            assertTrue(objects.contains(replacement))
            objects.timers.run()
        }
        assertTrue(objects.contains(original))
        assertFalse(objects.contains(replacement))
        verifyOrder {
            calls.add(replacement)
            events.emit(replacement, Despawn)
        }
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun tearDown() {
            Events.setEvents(Events())
        }
    }
}
