package content.area.kharidian_desert.al_kharid.duel_arena

import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import kotlin.test.assertTrue

class DuelArenaBankTest : WorldTest() {

    @Test
    fun `Bank at duel arena bank chest`() {
        val player = createPlayer(Tile(3381, 3268))
        val chest = GameObjects.find(Tile(3381, 3269), "bank_chest_duel_arena")
        player.objectOption(chest, "Bank")
        tick(2)

        assertTrue(player.hasOpen("bank"))
    }
}
