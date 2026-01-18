package world.gregs.voidps.engine.entity.obj

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declare
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectAddition
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectRemoval
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameObjectsTest : KoinMock() {

    private lateinit var objects: GameObjects
    private lateinit var spawns: MutableList<GameObject>
    private lateinit var despawns: MutableList<GameObject>

    @BeforeEach
    fun setup() {
        // Using collision = false to avoid koin [GameObject#def]
        ObjectDefinitions.set(arrayOf(ObjectDefinition(123), ObjectDefinition(456)), mapOf("test" to 123, "test2" to 456))
        mockkObject(ZoneBatchUpdates)
        objects = GameObjects(storeUnused = true)
        spawns = mockk(relaxed = true)
        despawns = mockk(relaxed = true)
        object : Spawn, Despawn {
            init {
                objectSpawn {
                    spawns.add(this)
                }
                objectDespawn {
                    despawns.add(this)
                }
            }
        }
    }

    @AfterEach
    fun teardown() {
        unmockkObject(ZoneBatchUpdates)
        Spawn.close()
        Despawn.close()
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
        verify(exactly = 0) {
            spawns.add(obj)
            despawns.add(obj)
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
            ZoneBatchUpdates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
        }
        verify(exactly = 0) {
            spawns.add(obj)
            despawns.add(obj)
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
            ZoneBatchUpdates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 1234, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
            spawns.add(obj)
            despawns.add(obj)
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
            ZoneBatchUpdates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 1234, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
            spawns.add(obj)
            ZoneBatchUpdates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 4321, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            spawns.add(override)
            ZoneBatchUpdates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            despawns.add(override)
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
            ZoneBatchUpdates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
            ZoneBatchUpdates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 1234, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            spawns.add(obj)
            ZoneBatchUpdates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            despawns.add(obj)
            ZoneBatchUpdates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 123, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
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
            ZoneBatchUpdates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
            ZoneBatchUpdates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 1234, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            spawns.add(obj)
            // Add 4321
            ZoneBatchUpdates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            despawns.add(obj)
            ZoneBatchUpdates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 4321, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            spawns.add(override)
            // Remove 4321
            ZoneBatchUpdates.add(obj.tile.zone, ObjectRemoval(tile = obj.tile.id, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
            despawns.add(override)
            ZoneBatchUpdates.add(obj.tile.zone, ObjectAddition(tile = obj.tile.id, id = 123, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1))
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
            spawns.add(obj)
            despawns.add(obj)
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
            spawns.add(obj)
            despawns.add(obj)
            spawns.add(obj)
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
            spawns.add(obj)
            despawns.add(obj)
            spawns.add(replacement)
            spawns.add(obj)
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
            spawns.add(replacement)
            despawns.add(replacement)
        }
    }
}
