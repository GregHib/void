package world.gregs.voidps.engine.entity.obj

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomObjectsTest {

    private lateinit var objects: CustomObjects
    private lateinit var gameObjects: GameObjects
    private lateinit var definitions: ObjectDefinitions

    @BeforeEach
    fun setup() {
        // Using collision false to avoid koin [GameObject#def]
        gameObjects = GameObjects(GameObjectCollision(Collisions()), ChunkBatchUpdates())
        definitions = mockk(relaxed = true)
        every { definitions.get("test") } returns ObjectDefinition(123)
        every { definitions.get("test2") } returns ObjectDefinition(456)
        objects = CustomObjects(gameObjects, definitions)
    }

    @Test
    fun `Spawned object is removed after ticks`() {
        val obj = objects.spawn(id = "test", tile = Tile(100, 100), type = 10, rotation = 2, ticks = 5, collision = false)
        repeat(5) {
            assertTrue(gameObjects.contains(obj))
            objects.run()
        }
        assertFalse(gameObjects.contains(obj))
    }

    @Test
    fun `Remove object is returned after ticks`() {
        val obj = GameObject(id = 123, x = 100, y = 100, plane = 0, type = 10, rotation = 2)
        gameObjects.add(obj, collision = false)
        objects.remove(original = obj, ticks = 5, collision = false)
        repeat(5) {
            assertFalse(gameObjects.contains(obj))
            objects.run()
        }
        assertTrue(gameObjects.contains(obj))
    }

    @Test
    fun `Replace object is undone after ticks`() {
        val obj = GameObject(id = 5678, x = 100, y = 100, plane = 0, type = 10, rotation = 2)
        gameObjects.add(obj, collision = false)
        val replacement = objects.replace(obj, "test", Tile(101, 100), 10, 1, ticks = 5, collision = false)
        repeat(5) {
            assertFalse(gameObjects.contains(obj))
            assertTrue(gameObjects.contains(replacement))
            objects.run()
        }
        assertTrue(gameObjects.contains(obj))
        assertFalse(gameObjects.contains(replacement))
    }

    @Test
    fun `Replace object pair is undone after ticks`() {
        val original1 = GameObject(id = 5678, x = 100, y = 100, plane = 0, type = 10, rotation = 2)
        val original2 = GameObject(id = 1234, x = 102, y = 102, plane = 1, type = 11, rotation = 1)
        gameObjects.add(original1, collision = false)
        gameObjects.add(original2, collision = false)
        objects.replace(original1, "test", Tile(101, 100), 1, original2, "test2", Tile(101, 101), 0, ticks = 5, collision = false)
        val replacement1 = GameObject(id = 123, x = 101, y = 100, plane = 0, type = 10, rotation = 1)
        val replacement2 = GameObject(id = 456, x = 101, y = 101, plane = 0, type = 11, rotation = 0)
        repeat(5) {
            assertFalse(gameObjects.contains(original1))
            assertFalse(gameObjects.contains(original2))
            assertTrue(gameObjects.contains(replacement1))
            assertTrue(gameObjects.contains(replacement2))
            objects.run()
        }
        assertTrue(gameObjects.contains(original1))
        assertTrue(gameObjects.contains(original2))
        assertFalse(gameObjects.contains(replacement1))
        assertFalse(gameObjects.contains(replacement2))
    }
}