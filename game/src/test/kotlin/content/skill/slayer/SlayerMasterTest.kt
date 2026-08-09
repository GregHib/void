package content.skill.slayer

import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SlayerMasterTest : WorldTest() {

    @Test
    fun `Get a task from the Canifis slayer master`() {
        val player = maxedPlayer(Tile(3511, 3510))
        val master = createNPC("mazchna_canifis", Tile(3511, 3509))

        player.npcOption(master, "Get-task")
        tick(2)

        assertEquals("mazchna", player.slayerMaster)
        assertNotEquals("nothing", player.slayerTask)
    }

    @Test
    fun `Get a task from the Shilo Village slayer master`() {
        val player = maxedPlayer(Tile(2869, 2983))
        val master = createNPC("duradel_shilo_village", Tile(2869, 2982))

        player.npcOption(master, "Get-task")
        tick(2)

        assertEquals("duradel", player.slayerMaster)
        assertNotEquals("nothing", player.slayerTask)
    }

    private fun maxedPlayer(tile: Tile): Player {
        val player = createPlayer(tile)
        for (skill in Skill.entries) {
            player.levels.set(skill, 99)
        }
        return player
    }
}
