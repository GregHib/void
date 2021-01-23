package world.gregs.voidps.cache.definition.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.MapTile.Companion.getHash
import world.gregs.voidps.cache.definition.data.MapTile.Companion.getHeight
import world.gregs.voidps.cache.definition.data.MapTile.Companion.getOpcode
import world.gregs.voidps.cache.definition.data.MapTile.Companion.getOverlay
import world.gregs.voidps.cache.definition.data.MapTile.Companion.getPath
import world.gregs.voidps.cache.definition.data.MapTile.Companion.getRotation
import world.gregs.voidps.cache.definition.data.MapTile.Companion.getSettings
import world.gregs.voidps.cache.definition.data.MapTile.Companion.getUnderlay

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