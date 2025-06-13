package world.gregs.voidps.cache.definition.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.MapTile.Companion.height
import world.gregs.voidps.cache.definition.data.MapTile.Companion.opcode
import world.gregs.voidps.cache.definition.data.MapTile.Companion.overlay
import world.gregs.voidps.cache.definition.data.MapTile.Companion.pack
import world.gregs.voidps.cache.definition.data.MapTile.Companion.path
import world.gregs.voidps.cache.definition.data.MapTile.Companion.rotation
import world.gregs.voidps.cache.definition.data.MapTile.Companion.settings
import world.gregs.voidps.cache.definition.data.MapTile.Companion.underlay

internal class MapTileTest {

    @Test
    fun `Get values from packed`() {
        val packed = pack(255, 49, 255, 11, 4, 32, 174)
        assertEquals(255, height(packed))
        assertEquals(49, opcode(packed))
        assertEquals(255, overlay(packed))
        assertEquals(11, path(packed))
        assertEquals(4, rotation(packed))
        assertEquals(32, settings(packed))
        assertEquals(174, underlay(packed))
    }
}
