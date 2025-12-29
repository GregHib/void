package world.gregs.voidps.engine.map.obj

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.buffer.write.ArrayWriter
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
        val writer = ArrayWriter()
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
        val writer = ArrayWriter()
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
    fun `Load object in offset zone`() {
        val writer = ArrayWriter()
        writer.writeSmart(124)
        writer.writeSmart(packTile(8, 56, 0))
        val shape = ObjectShape.GROUND_DECOR
        writer.writeByte(packInfo(shape, 0))
        writer.writeSmart(0)
        writer.writeSmart(0)
        val array = writer.toArray()

        decoder.zoneRotation = 0
        decoder.zone = Rectangle(8, 16, 56, 64)
        decoder.decode(array, settings, 8, 56)

        assertNull(objects.getShape(Tile(0, 56), shape))

        val tile = Tile(8, 56, 0)
        val gameObject = objects.getShape(tile, shape)

        assertNotNull(gameObject)
        assertEquals(shape, gameObject!!.shape)
        assertEquals(0, gameObject.rotation)
        assertEquals(123, gameObject.intId)
    }

    @Test
    fun `Load ignores objects out of zone`() {
        val writer = ArrayWriter()
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

    @TestFactory
    fun `No rotation`() = listOf(
        Pair(Tile(0, 0), Tile(0, 0)),
        Pair(Tile(3, 4), Tile(3, 4)),
        Pair(Tile(7, 7), Tile(7, 7)),
        Pair(Tile(0, 7), Tile(0, 7)),
        Pair(Tile(7, 0), Tile(7, 0))
    ).map { (tile, expected) ->
        dynamicTest("Rotate $tile") {
            val result = rotate(tile.x, tile.y, 1, 1, 0, 0)
            assertEquals(expected, result, "Failed for $tile")
        }
    }

    @Test
    fun `Zone rotation 90 degrees`() {
        assertEquals(Tile(0, 7), rotate(0, 0, 1, 1, 0, 1))
        assertEquals(Tile(4, 4), rotate(3, 4, 1, 1, 0, 1))
        assertEquals(Tile(7, 0), rotate(7, 7, 1, 1, 0, 1))
        assertEquals(Tile(0, 0), rotate(7, 0, 1, 1, 0, 1))
    }

    @Test
    fun `Zone rotation 180 degrees`() {
        assertEquals(Tile(7, 7), rotate(0, 0, 1, 1, 0, 2))
        assertEquals(Tile(4, 3), rotate(3, 4, 1, 1, 0, 2))
        assertEquals(Tile(0, 0), rotate(7, 7, 1, 1, 0, 2))
    }

    @Test
    fun `Zone rotation 270 degrees`() {
        assertEquals(Tile(7, 0), rotate(0, 0, 1, 1, 0, 3))
        assertEquals(Tile(3, 3), rotate(3, 4, 1, 1, 0, 3))
        assertEquals(Tile(0, 7), rotate(7, 7, 1, 1, 0, 3))
    }

    @Test
    fun `2x1 object - no rotation`() {
        val tile = rotate(0, 0, 2, 1, 0, 0)
        assertEquals(Tile.EMPTY, tile)
    }

    @Test
    fun `2x1 object - zone rotation 180`() {
        val tile = rotate(0, 0, 2, 1, 0, 2)
        assertEquals(Tile(6, 7), tile)
    }

    @Test
    fun `3x2 object - no rotation`() {
        val tile = rotate(2, 3, 3, 2, 0, 0)
        assertEquals(Tile(2, 3), tile)
    }

    @Test
    fun `3x2 object - zone rotation 180`() {
        val tile = rotate(2, 3, 3, 2, 0, 2)
        assertEquals(Tile(3, 3), tile)
    }

    @Test
    fun `1x2 object rotated 90 degrees - no zone rotation`() {
        val tile = rotate(0, 0, 1, 2, 1, 0)
        assertEquals(Tile.EMPTY, tile)
    }

    @Test
    fun `1x2 object rotated 90 degrees - zone rotation 180`() {
        val tile = rotate(0, 0, 1, 2, 1, 2)
        assertEquals(Tile(6, 7), tile)
    }


    @TestFactory
    fun `2x1 object with all rotation combinations`() = listOf(
        Triple(0, 0, Tile(0, 0)),
        Triple(1, 0, Tile(0, 0)),
        Triple(2, 0, Tile(0, 0)),
        Triple(3, 0, Tile(0, 0)),
        Triple(0, 1, Tile(0, 6)),
        Triple(0, 2, Tile(6, 7)),
        Triple(0, 3, Tile(7, 0))
    ).map { (objRot, zoneRot, expected) ->
        dynamicTest("2x1 object rotate obj $objRot zone rot $zoneRot") {
            val result = rotate(0, 0, 2, 1, objRot, zoneRot)
            assertEquals(expected, result, "Failed for objRot=$objRot, zoneRot=$zoneRot")
        }
    }

    @Test
    fun `Rotation at corners of zones`() {
        assertEquals(Tile(0, 0), rotate(0, 0, 1, 1, 0, 0))
        assertEquals(Tile(7, 0), rotate(7, 0, 1, 1, 0, 0))
        assertEquals(Tile(0, 7), rotate(0, 7, 1, 1, 0, 0))
        assertEquals(Tile(7, 7), rotate(7, 7, 1, 1, 0, 0))
    }

    @Test
    fun `Rotation values with masking`() {
        assertEquals(rotate(3, 3, 1, 1, 0, 0), rotate(3, 3, 1, 1, 4, 4))
        assertEquals(rotate(3, 3, 1, 1, 1, 1), rotate(3, 3, 1, 1, 5, 5))
    }

    @Test
    fun `Rotate large objects at boundaries`() {
        // 3x3 object at different positions
        val tile1 = rotate(0, 0, 3, 3, 0, 2)
        assertEquals(5, tile1.x)
        assertEquals(5, tile1.y)

        val tile2 = rotate(5, 5, 3, 3, 0, 2)
        assertEquals(0, tile2.x)
        assertEquals(0, tile2.y)
    }

    @Test
    fun `Double 180 rotation returns to original`() {
        val original = Tile(3, 4)
        val rotated = rotate(3, 4, 1, 1, 0, 2)
        val backAgain = rotate(rotated.x, rotated.y, 1, 1, 0, 2)
        assertEquals(original, backAgain)
    }

    private fun rotate(
        objX: Int, objY: Int,
        sizeX: Int, sizeY: Int,
        objRotation: Int, zoneRotation: Int
    ): Tile {
        val x = decoder.rotateX(objX, objY, sizeX, sizeY, objRotation, zoneRotation)
        val y = decoder.rotateY(objX, objY, sizeX, sizeY, objRotation, zoneRotation)
        return Tile(x, y)
    }
    companion object {
        private fun packInfo(shape: Int, rotation: Int) = rotation + (shape shl 2)

        private fun packTile(localX: Int, localY: Int, level: Int) = (localY and 0x3f) + (localX and 0x3f shl 6) + (level shl 12) + 1
    }
}
