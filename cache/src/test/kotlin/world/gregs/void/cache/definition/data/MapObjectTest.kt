package world.gregs.void.cache.definition.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.void.cache.definition.data.MapObject.Companion.getHash
import world.gregs.void.cache.definition.data.MapObject.Companion.getId
import world.gregs.void.cache.definition.data.MapObject.Companion.getPlane
import world.gregs.void.cache.definition.data.MapObject.Companion.getRotation
import world.gregs.void.cache.definition.data.MapObject.Companion.getType
import world.gregs.void.cache.definition.data.MapObject.Companion.getX
import world.gregs.void.cache.definition.data.MapObject.Companion.getY

internal class MapObjectTest {
    @Test
    fun `Get values from hash`() {
        val hash = getHash(43200, 12000, 9600, 3, 22, 3)
        assertEquals(43200, getId(hash))
        assertEquals(12000, getX(hash))
        assertEquals(9600, getY(hash))
        assertEquals(3, getPlane(hash))
        assertEquals(22, getType(hash))
        assertEquals(3, getRotation(hash))
    }
}