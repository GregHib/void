package world.gregs.voidps.engine.map.obj

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.type.Tile

class MapObjectsDecoderTest {

    private lateinit var definitions: ObjectDefinitions
    private lateinit var objects: GameObjects
    private lateinit var decoder: MapObjectsDecoder
    private lateinit var tiles: ByteArray

    @BeforeEach
    fun setup() {
        definitions = ObjectDefinitions(Array(10_000) { ObjectDefinition.EMPTY })
        objects = GameObjects(GameObjectCollisionAdd(), GameObjectCollisionRemove(), ZoneBatchUpdates(), definitions, storeUnused = true)
        decoder = MapObjectsDecoder(objects, definitions)
        tiles = ByteArray(64 * 64 * 4)
    }

    @Test
    fun `Load object`() {
        val writer = ArrayWriter()
        writer.writeSmart(124)
        writer.writeSmart(packTile(10, 11, 1))
        val shape = ObjectShape.GROUND_DECOR
        writer.writeByte(packInfo(shape, 2))
        writer.writeSmart(0)
        writer.writeSmart(0)
        val array = writer.toArray()
        decoder.decode(array, tiles, 128, 256)

        val tile = Tile(138, 267, 1)
        val gameObject = objects.getShape(tile, shape)

        assertNotNull(gameObject)
        assertEquals(shape, gameObject!!.shape)
        assertEquals(2, gameObject.rotation)
        assertEquals(123, gameObject.intId)
    }

    @Test
    fun `Load multiple object locations with same id`() {
        val writer = ArrayWriter()
        writer.writeSmart(124)
        writer.writeSmart(packTile(10, 11, 1))
        writer.writeByte(packInfo(ObjectShape.WALL_CORNER, 0))
        writer.writeSmart(packTile(4, 4, 1))
        writer.writeByte(packInfo(ObjectShape.WALL_STRAIGHT, 1))
        writer.writeSmart(0)
        writer.writeSmart(0)
        val array = writer.toArray()
        decoder.decode(array, tiles, 128, 256)

        var tile = Tile(138, 267, 1)
        var gameObject = objects.getShape(tile, ObjectShape.WALL_CORNER)
        assertNotNull(gameObject)
        assertEquals(0, gameObject!!.rotation)
        assertEquals(123, gameObject.intId)

        tile = Tile(142, 271, 2)
        gameObject = objects.getShape(tile, ObjectShape.WALL_STRAIGHT)
        assertNotNull(gameObject)
        assertEquals(1, gameObject!!.rotation)
        assertEquals(123, gameObject.intId)
    }

    @Test
    fun `Load multiple objects of different ids`() {
        val writer = ArrayWriter()
        writer.writeSmart(124)
        writer.writeSmart(packTile(10, 11, 0))
        writer.writeByte(packInfo(ObjectShape.GROUND_DECOR, 3))
        writer.writeSmart(0)
        writer.writeSmart(1234)
        writer.writeSmart(packTile(4, 8, 2))
        writer.writeByte(packInfo(ObjectShape.ROOF_DIAGONAL, 0))
        writer.writeSmart(0)
        writer.writeSmart(0)
        val array = writer.toArray()
        decoder.decode(array, tiles, 192, 64)

        var tile = Tile(202, 75, 0)
        var gameObject = objects.getShape(tile, ObjectShape.GROUND_DECOR)
        assertNotNull(gameObject)
        assertEquals(3, gameObject!!.rotation)
        assertEquals(123, gameObject.intId)

        tile = Tile(196, 72, 2)
        gameObject = objects.getShape(tile, ObjectShape.ROOF_DIAGONAL)
        assertNotNull(gameObject)
        assertEquals(0, gameObject!!.rotation)
        assertEquals(1357, gameObject.intId)
    }

    companion object {
        private fun packInfo(shape: Int, rotation: Int) = rotation + (shape shl 2)

        private fun packTile(localX: Int, localY: Int, level: Int) = (localY and 0x3f) + (localX and 0x3f shl 6) + (level shl 12) + 1
    }
}
