package world.gregs.voidps.cache.definition.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.Tile

class MapObjectTest {

    @Test
    fun `Get values from hash`() {
        val value = MapObject.pack(43200, 7, 6, 3, 22, 3)
        assertEquals(43200, MapObject.id(value))
        assertEquals(7, MapObject.x(value))
        assertEquals(6, MapObject.y(value))
        assertEquals(3, MapObject.plane(value))
        assertEquals(22, MapObject.shape(value))
        assertEquals(3, MapObject.rotation(value))
    }

    @Test
    fun `Get tile from hash`() {
        val value = MapObject.pack(43200, 7, 6, 3, 22, 3)
        val tile = MapObject.tile(value)
        assertEquals(7, Tile.indexX(tile))
        assertEquals(6, Tile.indexY(tile))
    }

    @Test
    fun `Get info from hash`() {
        val value = MapObject.pack(51213, 0, 0, 0, 1, 1)
        val info = MapObject.info(value)
        assertEquals(51213, MapObject.infoId(info))
        assertEquals(1, MapObject.infoShape(info))
        assertEquals(1, MapObject.infoRotation(info))
    }
}