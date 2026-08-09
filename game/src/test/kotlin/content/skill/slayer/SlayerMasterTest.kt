package content.skill.slayer

import WorldTest
import content.skill.slayer.master.strongerMaster
import dialogueContinue
import dialogueOption
import net.pearx.kasechange.toSentenceCase
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

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

    @Test
    fun `Duradel assigns a task instead of sending you to himself`() {
        val player = maxedPlayer(Tile(2869, 2983))
        val master = createNPC("duradel_shilo_village", Tile(2869, 2982))

        player.npcOption(master, "Talk-to")
        tick(2)
        player.dialogueContinue()
        player.dialogueOption("line1") // I need another assignment.
        player.dialogueContinue()

        assertEquals("duradel", player.slayerMaster)
        assertNotEquals("nothing", player.slayerTask)
    }

    @Test
    fun `Each master defers to the next master up`() {
        val ladder = listOf("turael", "mazchna", "vannaka", "chaeldar", "sumona", "duradel", "kuradal")
        for ((index, master) in ladder.withIndex()) {
            val stronger = strongerMaster(master)
            if (master == "duradel" || master == "kuradal") {
                assertNull(stronger, "$master has no stronger master to defer to")
                continue
            }
            assertEquals(ladder[index + 1].toSentenceCase(), stronger?.second?.substringBefore(" in"))
        }
    }

    private fun maxedPlayer(tile: Tile): Player {
        val player = createPlayer(tile)
        for (skill in Skill.entries) {
            player.levels.set(skill, 99)
        }
        return player
    }
}
