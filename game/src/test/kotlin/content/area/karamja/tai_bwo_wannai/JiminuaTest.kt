package content.area.karamja.tai_bwo_wannai

import WorldTest
import npcOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.type.Tile

class JiminuaTest : WorldTest() {

    @Test
    fun `Trade with Jiminua opens her store`() {
        val player = createPlayer(Tile(2767, 3122))
        val jiminua = createNPC("jiminua_tai_bwo_wannai", Tile(2766, 3122))

        player.npcOption(jiminua, "Trade")
        tick(2)

        assertTrue(player.hasOpen("shop"))
    }
}
