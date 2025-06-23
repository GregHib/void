package world.gregs.voidps.cache.definition.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.MapObject.Companion.id
import world.gregs.voidps.cache.definition.data.MapObject.Companion.level
import world.gregs.voidps.cache.definition.data.MapObject.Companion.pack
import world.gregs.voidps.cache.definition.data.MapObject.Companion.rotation
import world.gregs.voidps.cache.definition.data.MapObject.Companion.shape
import world.gregs.voidps.cache.definition.data.MapObject.Companion.x
import world.gregs.voidps.cache.definition.data.MapObject.Companion.y

internal class MapObjectTest {
    @Test
    fun `Get values from packed`() {
        val packed = pack(43200, 12000, 9600, 3, 22, 3)
        assertEquals(43200, id(packed))
        assertEquals(12000, x(packed))
        assertEquals(9600, y(packed))
        assertEquals(3, level(packed))
        assertEquals(22, shape(packed))
        assertEquals(3, rotation(packed))
    }
}
