package content.bot

import WorldTest
import content.bot.behaviour.condition.BotSkillLevel
import content.entity.player.bank.bank
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

/** Real game ticks, definitions, navigation, banking, tree depletion and XP awards. */
class MapleTrainingWorldTest : WorldTest() {
    @Test
    fun `Level one bot trains a private maple goal at twenty times normal XP`() {
        Settings.load(mapOf("world.experienceRate" to "20.0", "bots.spawnRequirements" to "false"))
        setRandom(Random(24045))
        val player = createPlayer(Tile(3080, 3267), "maple_trainee") {
            // Owned starter tools only. No XP, levels, logs or replacement tools are granted during training.
            assertTrue(it.inventory.add("bronze_hatchet"))
            assertTrue(it.bank.add("steel_hatchet"))
        }
        val bot = Bot(player)
        player["bot"] = bot
        val manager = get<BotManager>()
        val maples = manager.activity("seers_village_maple_trees")!!
        val allowed = setOf(maples.id, "draynor_north_trees", "draynor_west_oak_trees", "draynor_willow_trees")
        // Keep this experiment on one reproducible training route, using the unmodified live definitions.
        bot.blocked.addAll(manager.activityNames - allowed)
        manager.add(bot)
        val adapted = manager.adaptSkillGoal(bot, maples)!!
        assertTrue(manager.assign(bot, adapted))
        assertEquals(1, player.levels.getMax(Skill.Woodcutting))
        assertEquals(0.0, player.experience.get(Skill.Woodcutting))
        assertEquals(20.0, Settings["world.experienceRate", 1.0])
        assertFalse(Settings["bots.spawnRequirements", true])
        assertTrue(adapted.setup.any { it is BotSkillLevel && it.skill == Skill.Woodcutting && it.min == 45 })
        assertTrue(maples.requires.any { it is BotSkillLevel && it.min == 45 })

        var lastCheckpoint = ""
        var goalReachedAt: Int? = null
        var ticks = 0
        val trainingBatches = mutableSetOf<String>()
        while (ticks < 10_000 && player.inventory.count("maple_logs") == 0) {
            tick()
            ticks++
            val level = player.levels.getMax(Skill.Woodcutting)
            if (level >= 45 && goalReachedAt == null) goalReachedAt = ticks
            bot.frames.map { it.behaviour.id }.filterTo(trainingBatches) { it in allowed && it != maples.id }
            val checkpoint = "level=$level frames=${bot.frames.map { "${it.behaviour.id}:${it.state::class.simpleName}" }}"
            if (checkpoint != lastCheckpoint) {
                println("MAPLE_TRAINING tick=$ticks xp=${player.experience.get(Skill.Woodcutting)} tile=${player.tile} $checkpoint")
                lastCheckpoint = checkpoint
            }
            assertSame(bot.frames.firstOrNull()?.behaviour, bot.previous, "Parent selection changed: $checkpoint")
            assertFalse(bot.noTask(), "Training stopped: $checkpoint goalReachedAt=$goalReachedAt")
            assertFalse(Settings["bots.spawnRequirements", true])
        }

        val logs = mapOf("logs" to 25.0, "oak_logs" to 37.5, "willow_logs" to 67.5, "maple_logs" to 100.0)
        val counts = logs.mapValues { (id, _) -> player.bank.count(id) + player.inventory.count(id) }
        val earnedXp = logs.entries.sumOf { (id, xp) -> counts.getValue(id) * xp * 20.0 }
        println("MAPLE_TRAINING result ticks=$ticks goalReachedAt=$goalReachedAt level=${player.levels.getMax(Skill.Woodcutting)} xp=${player.experience.get(Skill.Woodcutting)} batches=$trainingBatches logs=$counts")
        assertNotNull(goalReachedAt, "Did not reach base Woodcutting 45")
        assertEquals(earnedXp, player.experience.get(Skill.Woodcutting), "All XP must be accounted for by real logs at 20x")
        assertTrue(trainingBatches.containsAll(allowed - maples.id), "Expected normal tree, oak and willow batches")
        assertTrue(player.inventory.contains("maple_logs"), "The maple parent did not resume and chop a log")
        assertSame(maples, manager.activity(maples.id), "Shared maple definition changed")
        assertEquals(maples.id, bot.previous!!.id)
        manager.remove(bot)
        assertTrue(manager.slots.hasFree(adapted))
    }
}
