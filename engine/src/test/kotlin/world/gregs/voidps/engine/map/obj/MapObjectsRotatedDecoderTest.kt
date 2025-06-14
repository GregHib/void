package world.gregs.voidps.engine.map.obj

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

class MapObjectsRotatedDecoderTest {

    private lateinit var definitions: ObjectDefinitions
    private lateinit var objects: GameObjects
    private lateinit var decoder: MapObjectsRotatedDecoder
    private lateinit var settings: ByteArray

    @BeforeEach
    fun setup() {
        definitions = ObjectDefinitions(Array(10_000) { ObjectDefinition.EMPTY })
        val collisions = Collisions()
        objects = GameObjects(GameObjectCollisionAdd(collisions), GameObjectCollisionRemove(collisions), ZoneBatchUpdates(), definitions, storeUnused = true)
        decoder = MapObjectsRotatedDecoder(objects, definitions)
        settings = ByteArray(64 * 64 * 4)
    }

    @Test
    fun `Load object in moved zone`() {
        val writer = BufferWriter()
        writer.writeSmart(124)
        writer.writeSmart(packTile(10, 11, 1))
        val shape = ObjectShape.GROUND_DECOR
        writer.writeByte(packInfo(shape, 2))
        writer.writeSmart(0)
        writer.writeSmart(0)
        val array = writer.toArray()

        decoder.zoneRotation = 2
        decoder.zone = Rectangle(8, 8, 16, 16)
        decoder.decode(array, settings, 960, 896)

        val tile = Tile(965, 900, 1) // local 5, 4
        val gameObject = objects.getShape(tile, shape)

        assertNotNull(gameObject)
        assertEquals(shape, gameObject!!.shape)
        assertEquals(0, gameObject.rotation)
        assertEquals(123, gameObject.intId)
    }

    @Test
    fun `Load object in rotated zone`() {
        val writer = BufferWriter()
        writer.writeSmart(124)
        writer.writeSmart(packTile(2, 3, 0))
        val shape = ObjectShape.GROUND_DECOR
        writer.writeByte(packInfo(shape, 2))
        writer.writeSmart(0)
        writer.writeSmart(0)
        val array = writer.toArray()

        decoder.zoneRotation = 3
        decoder.zone = Rectangle(0, 0, 8, 8)
        decoder.decode(array, settings, 0, 0)

        val tile = Tile(4, 2, 0)
        val gameObject = objects.getShape(tile, shape)

        assertNotNull(gameObject)
        assertEquals(shape, gameObject!!.shape)
        assertEquals(1, gameObject.rotation)
        assertEquals(123, gameObject.intId)
    }

    @Test
    fun `Load ignores objects out of zone`() {
        val writer = BufferWriter()
        writer.writeSmart(124)
        writer.writeSmart(packTile(18, 19, 0))
        val shape = ObjectShape.ROOF_EDGE_CORNER
        writer.writeByte(packInfo(shape, 1))
        writer.writeSmart(0)
        writer.writeSmart(0)
        val array = writer.toArray()

        decoder.zoneRotation = 0
        decoder.zone = Rectangle(8, 8, 16, 16)
        decoder.decode(array, settings, 64, 64)

        val tile = Tile(82, 84, 0)
        val gameObject = objects.getShape(tile, shape)
        assertNull(gameObject)
    }

    companion object {
        private fun packInfo(shape: Int, rotation: Int) = rotation + (shape shl 2)

        private fun packTile(localX: Int, localY: Int, level: Int) = (localY and 0x3f) + (localX and 0x3f shl 6) + (level shl 12) + 1
    }
}
