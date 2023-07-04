package world.gregs.voidps.cache.definition.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.MapDefinition.Companion.index
import world.gregs.voidps.cache.definition.data.MapDefinition.Companion.localX
import world.gregs.voidps.cache.definition.data.MapDefinition.Companion.localY
import world.gregs.voidps.cache.definition.data.MapDefinition.Companion.plane

internal class MapDefinitionTest {

    @Test
    fun `Get values from index`() {
        val index = index(54, 63, 3)
        assertEquals(54, localX(index))
        assertEquals(63, localY(index))
        assertEquals(3, plane(index))
    }
}