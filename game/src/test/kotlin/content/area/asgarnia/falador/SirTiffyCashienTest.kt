package content.area.asgarnia.falador

import WorldTest
import content.entity.death.respawnTile
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class SirTiffyCashienTest : WorldTest() {

    @Test
    fun `Sir Tiffy changes the respawn point`() {
        val player = createPlayer(emptyTile)
        val tiffy = createNPC("sir_tiffy_cashien", player.tile.addY(1))

        player.npcOption(tiffy, 0)
        tick()
        player.dialogueContinue()
        player.dialogueOption(1)
        player.dialogueContinue()
        player.dialogueOption(4)

        assertEquals(Tile(3087, 3497), player.respawnTile())
    }
}
