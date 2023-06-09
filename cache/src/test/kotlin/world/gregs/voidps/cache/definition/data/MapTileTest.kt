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
    fun `Get values from hash`() {
        val hash = pack(255, 49, 255, 11, 4, 32, 174)
        assertEquals(255, height(hash))
        assertEquals(49, opcode(hash))
        assertEquals(255, overlay(hash))
        assertEquals(11, path(hash))
        assertEquals(4, rotation(hash))
        assertEquals(32, settings(hash))
        assertEquals(174, underlay(hash))
    }
}