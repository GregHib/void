package content.skill.slayer

import FakeRandom
import WorldTest
import content.entity.death.NPCDeath
import dialogueContinue
import dialogueOption
import interfaceOption
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SlayerAssignmentTest : WorldTest() {

    /**
     * Every task [master] can give, by stepping through each possible weighted roll.
     */
    private fun Player.possibleTasks(master: String): Set<String> {
        val tasks = mutableSetOf<String>()
        for (i in 0 until 500) {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int): Int = i % until
            })
            slayerTask = "nothing"
            slayerLastTask = "nothing"
            tasks.add(assignTask(this, master)?.first ?: continue)
        }
        return tasks
    }

    @Test
    fun `Tasks aren't assigned without their quest`() {
        val player = maxedPlayer(Tile(3200, 3200))

        assertFalse("banshees" in player.possibleTasks("turael"))

        player["priest_in_peril"] = "completed"
        assertTrue("banshees" in player.possibleTasks("turael"))
    }

    @Test
    fun `Blocked tasks are never assigned`() {
        val player = createPlayer(Tile(3200, 3200))
        player["blocked_task_0"] = "birds"

        assertFalse("birds" in player.possibleTasks("turael"))
    }

    @Test
    fun `The same task isn't assigned twice in a row`() {
        val player = createPlayer(Tile(3200, 3200))
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        val first = assignTask(player, "turael")!!.first
        player.slayerTask = "nothing"
        val second = assignTask(player, "turael")!!.first

        assertNotEquals(first, second)
    }

    @Test
    fun `No task is assigned when every task is unavailable`() {
        val player = createPlayer(Tile(3200, 3200))

        assertNull(assignTask(player, "unknown"))
    }

    @Test
    fun `Masters won't assign tasks to players who aren't strong enough`() {
        val player = createPlayer(Tile(2869, 2983))
        player["shilo_village"] = "completed"
        val master = createNPC("duradel_shilo_village", Tile(2869, 2982))

        player.npcOption(master, "Get-task")
        tick(2)

        assertEquals("nothing", player.slayerTask)
    }

    @Test
    fun `Masters other than Turael won't swap an existing task`() {
        val player = maxedPlayer(Tile(3511, 3510))
        player.slayerMaster = "vannaka"
        player.slayerTask = "dust_devils"
        player.slayerTaskRemaining = 50
        val master = createNPC("mazchna_canifis", Tile(3511, 3509))

        player.npcOption(master, "Get-task")
        tick(2)
        player.dialogueContinue()

        assertEquals("dust_devils", player.slayerTask)
        assertEquals(50, player.slayerTaskRemaining)
    }

    @Test
    fun `Turael swaps tasks he wouldn't give for an easier one at the cost of the streak`() {
        val player = maxedPlayer(Tile(2931, 3537))
        player.slayerMaster = "vannaka"
        player.slayerTask = "dust_devils"
        player.slayerStreak = 12
        val master = createNPC("turael", Tile(2931, 3536))

        player.npcOption(master, "Get-task")
        tick(2)
        player.dialogueContinue(3)
        player.dialogueOption("line1") // Yes, please.
        player.dialogueContinue()

        assertEquals("turael", player.slayerMaster)
        assertNotEquals("dust_devils", player.slayerTask)
        assertEquals(0, player.slayerStreak)
    }

    @Test
    fun `Turael won't swap a task he would give himself`() {
        val player = maxedPlayer(Tile(2931, 3537))
        player.slayerMaster = "vannaka"
        player.slayerTask = "birds"
        player.slayerStreak = 12
        val master = createNPC("turael", Tile(2931, 3536))

        player.npcOption(master, "Get-task")
        tick(2)
        player.dialogueContinue()

        assertEquals("birds", player.slayerTask)
        assertEquals(12, player.slayerStreak)
    }

    @Test
    fun `No points are earned for the first four tasks`() {
        val player = createPlayer(Tile(3200, 3200))
        player["smoking_kills"] = "completed"
        player.slayerMaster = "duradel"
        player.slayerTask = "birds"
        player.slayerStreak = 3
        player.slayerTaskRemaining = 1

        slay(player, createNPC("chicken", player.tile.addY(1)))

        assertEquals(4, player.slayerStreak)
        assertEquals(0, player.slayerPoints)
    }

    @Test
    fun `No points are earned before Smoking Kills`() {
        val player = createPlayer(Tile(3200, 3200))
        player.slayerMaster = "duradel"
        player.slayerTask = "birds"
        player.slayerStreak = 6
        player.slayerTaskRemaining = 1

        slay(player, createNPC("chicken", player.tile.addY(1)))

        assertEquals(7, player.slayerStreak)
        assertEquals(0, player.slayerPoints)
    }

    @Test
    fun `Block and unblock a task`() {
        val player = createPlayer(Tile(3200, 3200))
        player["quest_points"] = 50
        player.slayerPoints = 100
        player.slayerTask = "birds"
        player.slayerTaskRemaining = 20
        player.open("slayer_rewards_assignment")

        player.interfaceOption("slayer_rewards_assignment", "block_points", "Permanently Remove Current")

        assertEquals("birds", player["blocked_task_0", "nothing"])
        assertEquals("nothing", player.slayerTask)
        assertEquals(0, player.slayerPoints)

        player.interfaceOption("slayer_rewards_assignment", "cancel_1", "Cancel Removal")

        assertFalse(player.contains("blocked_task_0"))
    }

    @Test
    fun `Blocking a task needs 50 quest points per slot`() {
        val player = createPlayer(Tile(3200, 3200))
        player["quest_points"] = 99
        player["blocked_task_0"] = "cows"
        player.slayerPoints = 100
        player.slayerTask = "birds"
        player.open("slayer_rewards_assignment")

        player.interfaceOption("slayer_rewards_assignment", "block_points", "Permanently Remove Current")

        assertFalse(player.contains("blocked_task_1"))
        assertEquals("birds", player.slayerTask)
        assertEquals(100, player.slayerPoints)
    }

    @Test
    fun `Reassigning a task keeps the streak`() {
        val player = createPlayer(Tile(3200, 3200))
        player.slayerPoints = 30
        player.slayerStreak = 9
        player.slayerTask = "birds"
        player.slayerTaskRemaining = 20
        player.open("slayer_rewards_assignment")

        player.interfaceOption("slayer_rewards_assignment", "reassign_points", "Reassign Current")

        assertEquals("nothing", player.slayerTask)
        assertEquals(0, player.slayerTaskRemaining)
        assertEquals(9, player.slayerStreak)
        assertEquals(0, player.slayerPoints)
    }

    private fun slay(player: Player, npc: NPC) {
        scripts.filterIsInstance<NPCDeath>().first().slay(player, npc)
    }

    private fun maxedPlayer(tile: Tile): Player {
        val player = createPlayer(tile)
        for (skill in Skill.entries) {
            player.levels.set(skill, skill.maximum())
            player.experience.set(skill, Level.experience(skill, skill.maximum()))
        }
        return player
    }
}
