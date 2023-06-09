package world.gregs.voidps.cache.definition.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.MapObject.Companion.id
import world.gregs.voidps.cache.definition.data.MapObject.Companion.pack
import world.gregs.voidps.cache.definition.data.MapObject.Companion.plane
import world.gregs.voidps.cache.definition.data.MapObject.Companion.rotation
import world.gregs.voidps.cache.definition.data.MapObject.Companion.type
import world.gregs.voidps.cache.definition.data.MapObject.Companion.x
import world.gregs.voidps.cache.definition.data.MapObject.Companion.y

internal class MapObjectTest {
    @Test
    fun `Get values from hash`() {
        val hash = pack(43200, 12000, 9600, 3, 22, 3)
        assertEquals(43200, id(hash))
        assertEquals(12000, x(hash))
        assertEquals(9600, y(hash))
        assertEquals(3, plane(hash))
        assertEquals(22, type(hash))
        assertEquals(3, rotation(hash))
    }
}