package world.gregs.voidps.cache.definition.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.MapDefinition.Companion.getLocalX
import world.gregs.voidps.cache.definition.data.MapDefinition.Companion.getLocalY
import world.gregs.voidps.cache.definition.data.MapDefinition.Companion.getPlane
import world.gregs.voidps.cache.definition.data.MapDefinition.Companion.index

internal class MapDefinitionTest {

    @Test
    fun `Get values from index`() {
        val index = index(63, 63, 3)
        assertEquals(63, getLocalX(index))
        assertEquals(63, getLocalY(index))
        assertEquals(3, getPlane(index))
    }
}