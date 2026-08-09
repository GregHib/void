package content.skill.slayer

import WorldTest
import content.skill.slayer.master.strongerMaster
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
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
    fun `Kuradal assigns a task instead of sending you elsewhere`() {
        val player = maxedPlayer(Tile(3200, 3200))
        val master = createNPC("kuradal", Tile(3200, 3201))

        player.npcOption(master, "Talk-to")
        tick(2)
        player.dialogueContinue()
        player.dialogueOption("line1") // I need another assignment.
        player.dialogueContinue()

        assertEquals("kuradal", player.slayerMaster)
        assertNotEquals("nothing", player.slayerTask)
    }

    @Test
    fun `Every master points at the strongest one you qualify for`() {
        val player = maxedPlayer(Tile(3200, 3200))
        player["smoking_kills"] = "completed"

        for (master in listOf("turael", "mazchna", "vannaka", "chaeldar", "sumona", "duradel")) {
            assertEquals("kuradal", player.strongerMaster(master)?.id, "$master should point at Kuradal")
        }
        assertNull(player.strongerMaster("kuradal"))
    }

    @Test
    fun `The strongest master is the best one you can be assigned tasks by`() {
        val player = createPlayer(Tile(3200, 3200))
        player.setLevel(Skill.Attack, 80)
        player.setLevel(Skill.Strength, 80)
        player.setLevel(Skill.Defence, 60)
        player.setLevel(Skill.Constitution, 60)
        player.setLevel(Skill.Slayer, 55)

        // Combat 70 for Chaeldar, but Smoking Kills is outstanding and Duradel wants combat 100
        assertEquals("chaeldar", player.strongerMaster("turael")?.id)
        assertNull(player.strongerMaster("chaeldar"))
    }

    @Test
    fun `Finishing Smoking Kills promotes Sumona over Chaeldar`() {
        val player = createPlayer(Tile(3200, 3200))
        player.setLevel(Skill.Attack, 80)
        player.setLevel(Skill.Strength, 80)
        player.setLevel(Skill.Defence, 60)
        player.setLevel(Skill.Constitution, 60)
        player.setLevel(Skill.Slayer, 55)
        player["smoking_kills"] = "completed"

        assertEquals("sumona", player.strongerMaster("turael")?.id)
    }

    @Test
    fun `A weak player is left with their current master`() {
        val player = createPlayer(Tile(3200, 3200))

        assertNull(player.strongerMaster("turael"))
    }

    private fun maxedPlayer(tile: Tile): Player {
        val player = createPlayer(tile)
        for (skill in Skill.entries) {
            player.setLevel(skill, 99)
        }
        return player
    }

    private fun Player.setLevel(skill: Skill, level: Int) {
        experience.set(skill, Level.experience(level))
        levels.clear(skill)
    }
}
