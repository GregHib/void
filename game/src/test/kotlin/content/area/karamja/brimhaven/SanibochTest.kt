package content.area.karamja.brimhaven

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class SanibochTest : WorldTest() {

    @Test
    fun `Can't enter the dungeon without paying`() {
        val player = createPlayer(Tile(2745, 3152))
        val entrance = GameObjects.find(Tile(2743, 3153), "brimhaven_dungeon_entrance")

        player.objectOption(entrance, "Enter")
        tick(3)

        assertEquals(Tile(2745, 3152), player.tile)
        assertFalse(player.tile.y > 6400)
    }

    @Test
    fun `Paying lets the player enter the dungeon once`() {
        val player = createPlayer(Tile(2745, 3152))
        player["temporary_saniboch_access"] = true
        val entrance = GameObjects.find(Tile(2743, 3153), "brimhaven_dungeon_entrance")

        player.objectOption(entrance, "Enter")
        tick(3)

        assertEquals(Tile(2712, 9564), player.tile)
        assertFalse(player["temporary_saniboch_access", false])
        assertFalse(player.containsMessage("without paying"))
    }
}
