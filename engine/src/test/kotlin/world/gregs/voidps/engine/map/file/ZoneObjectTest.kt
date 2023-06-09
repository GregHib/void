package world.gregs.voidps.engine.map.file

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.Tile

class ZoneObjectTest {

    @Test
    fun `Get values from hash`() {
        val value = ZoneObject.pack(43200, 7, 6, 3, 22, 3)
        assertEquals(43200, ZoneObject.id(value))
        assertEquals(7, ZoneObject.x(value))
        assertEquals(6, ZoneObject.y(value))
        assertEquals(3, ZoneObject.plane(value))
        assertEquals(22, ZoneObject.type(value))
        assertEquals(3, ZoneObject.rotation(value))
    }

    @Test
    fun `Get tile from hash`() {
        val value = ZoneObject.pack(43200, 7, 6, 3, 22, 3)
        val tile = ZoneObject.tile(value)
        assertEquals(7, Tile.indexX(tile))
        assertEquals(6, Tile.indexY(tile))
    }

    @Test
    fun `Get info from hash`() {
        val value = ZoneObject.pack(51213, 0, 0, 0, 1, 1)
        val info = ZoneObject.info(value)
        assertEquals(51213, ZoneObject.infoId(info))
        assertEquals(1, ZoneObject.infoType(info))
        assertEquals(1, ZoneObject.infoRotation(info))
    }
}