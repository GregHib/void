package rs.dusk.cache.definition.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.cache.definition.data.MapTile.Companion.getHash
import rs.dusk.cache.definition.data.MapTile.Companion.getHeight
import rs.dusk.cache.definition.data.MapTile.Companion.getOpcode
import rs.dusk.cache.definition.data.MapTile.Companion.getOverlay
import rs.dusk.cache.definition.data.MapTile.Companion.getPath
import rs.dusk.cache.definition.data.MapTile.Companion.getRotation
import rs.dusk.cache.definition.data.MapTile.Companion.getSettings
import rs.dusk.cache.definition.data.MapTile.Companion.getUnderlay

internal class MapTileTest {

    @Test
    fun `Get values from hash`() {
        val hash = getHash(255, 49, 255, 11, 4, 32, 174)
        assertEquals(255, getHeight(hash))
        assertEquals(49, getOpcode(hash))
        assertEquals(255, getOverlay(hash))
        assertEquals(11, getPath(hash))
        assertEquals(4, getRotation(hash))
        assertEquals(32, getSettings(hash))
        assertEquals(174, getUnderlay(hash))
    }
}