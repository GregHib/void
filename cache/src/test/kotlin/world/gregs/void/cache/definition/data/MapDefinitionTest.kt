package world.gregs.void.cache.definition.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.void.cache.definition.data.MapDefinition.Companion.getHash
import world.gregs.void.cache.definition.data.MapDefinition.Companion.getLocalX
import world.gregs.void.cache.definition.data.MapDefinition.Companion.getLocalY
import world.gregs.void.cache.definition.data.MapDefinition.Companion.getPlane

internal class MapDefinitionTest {

    @Test
    fun `Get values from hash`() {
        val hash = getHash(63, 63, 3)
        assertEquals(63, getLocalX(hash))
        assertEquals(63, getLocalY(hash))
        assertEquals(3, getPlane(hash))
    }
}