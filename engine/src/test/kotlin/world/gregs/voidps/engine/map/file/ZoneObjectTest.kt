package world.gregs.voidps.engine.map.file

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.GameObjectCollision

class ZoneObjectTest {

    @Test
    fun `Get values from hash`() {
        val value = ZoneObject.value(43200, 7, 6, 3, 22, 3)
        assertEquals(43200, ZoneObject.id(value))
        assertEquals(7, ZoneObject.x(value))
        assertEquals(6, ZoneObject.y(value))
        assertEquals(3, ZoneObject.plane(value))
        assertEquals(22, ZoneObject.type(value))
        assertEquals(3, ZoneObject.rotation(value))
    }

    @Test
    fun `Get tile from hash`() {
        val value = ZoneObject.value(43200, 7, 6, 3, 22, 3)
        val tile = ZoneObject.tile(value)
        assertEquals(7, ZoneObject.tileX(tile))
        assertEquals(6, ZoneObject.tileY(tile))
    }

    @Test
    fun `Get info from hash`() {
        val value = ZoneObject.value(51213, 0, 0, 0, 1, 1)
        val info = ZoneObject.info(value)
        assertEquals(51213, ZoneObject.infoId(info))
        assertEquals(1, ZoneObject.infoType(info))
        assertEquals(1, ZoneObject.infoRotation(info))
    }

    @Test
    fun `Zone values`() {
        val value = ZoneObject.value(0, 7, 6, 0, 1, 1)
        val chunk = Chunk(1, 2)
        val zone = GameObjectCollision.zoneIndex(chunk.x, chunk.y, chunk.plane)
        val tile = ZoneObject.tile(value)
        assertEquals(7, ZoneObject.tileX(tile))
        assertEquals(6, ZoneObject.tileY(tile))
        assertEquals(1, GameObjectCollision.zoneX(zone))
        assertEquals(2, GameObjectCollision.zoneY(zone))
        assertEquals(8, GameObjectCollision.tileX(zone))
        assertEquals(16, GameObjectCollision.tileY(zone))
        assertEquals(15, GameObjectCollision.tileX(zone) + ZoneObject.tileX(tile))
        assertEquals(22, GameObjectCollision.tileY(zone) + ZoneObject.tileY(tile))
    }

    @Test
    fun `Tile addition`() {
        val value = ZoneObject.value(0, 0, 7, 0, 1, 1)
        val tile = ZoneObject.tile(value)
        val added = GameObjectCollision.addTile(tile, 0, 1)
        assertEquals(0, ZoneObject.tileX(added))
        assertEquals(0, ZoneObject.tileY(added))
    }

    @Test
    fun `Zone addition`() {
        val value = ZoneObject.value(0, 0, 7, 0, 1, 1)
        val tile = ZoneObject.tile(value)
        val chunk = Chunk(1, 2)

        val x = 0
        val y = 1
        var zone = GameObjectCollision.zoneIndex(chunk.x, chunk.y, chunk.plane)
        val tileX = ZoneObject.tileX(tile)
        val tileY = ZoneObject.tileY(tile)
        println("Tile $tileX $tileY")
        val remX = tileX + x shr 3
        val remY = tileY + y shr 3
        println("Rem $remX $remY")
        zone += (remX) or (remY shl 12)
        println(GameObjectCollision.zoneX(zone))
        println(GameObjectCollision.zoneY(zone))
//        val added = GameObjectCollision.addZone(zone, tile, 0, 1)
//        println(added)
//        assertEquals(0, ZoneObject.tileX(added))
//        assertEquals(0, ZoneObject.tileY(added))
    }
}