package content.bot

import FakeRandom
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.action.BotInteractNpc
import content.bot.behaviour.action.BotInteractObject
import content.bot.behaviour.action.BotRestart
import content.bot.behaviour.action.BotWait
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotInArea
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.BotSkillLevel
import content.bot.behaviour.condition.BotVariable
import content.bot.behaviour.setup.Resolver
import content.bot.behaviour.setup.TrainingResolver
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.type.setRandom

class TrainingResolverTest {
    private val helper = BotManagerTest()
    private val skill = Skill.Woodcutting
    private val goal = BotSkillLevel(skill, min = 60, max = 75)
    private val full = BotInventorySetup(listOf(BotItem(setOf("empty"), max = 0)))
    private val parent = BotActivity("yews", requires = listOf(goal), actions = listOf(BotWait(1)))

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
    }

    private fun batch(id: String = "trees", min: Int = 1, max: Int? = 15, area: String? = null) = BotActivity(
        id,
        requires = listOf(BotSkillLevel(skill, min, max)),
        setup = area?.let { listOf(BotInArea(it)) }.orEmpty(),
        actions = listOf(BotInteractObject("Chop down", "tree*", success = full)),
        produces = setOf("skill:woodcutting"),
    )

    private fun level(bot: Bot, level: Int) {
        bot.player.experience.set(skill, Level.experience(skill, level))
    }

    private fun testBot() = helper.testBot().also {
        it.player.inventories.player = it.player
        it.player.inventories.validItemRule = ValidItemRestriction()
        it.player.inventories.normalStack = ItemDependentStack
        it.player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 28))
    }

    private fun setArea() {
        Areas.set(mapOf("trees" to AreaDefinition("trees", Rectangle(0, 0, 10, 10), params = mapOf(Params.BOT_CAPACITY to 1))))
    }

    private fun startBatch(manager: BotManager, bot: Bot) {
        manager.tick(bot) // Parent setup queues resolver.
        assertEquals("train_skill", bot.frame().behaviour.id)
        manager.tick(bot) // Resolver schedules the ordinary activity.
        assertEquals(3, bot.frames.size)
    }

    private fun finishBatch(manager: BotManager, bot: Bot) {
        bot.frame().state = BehaviourState.Success
        manager.tick(bot) // Release child and return to resolver.
        manager.tick(bot) // Resolver succeeds.
        manager.tick(bot) // Return to pending parent.
        assertEquals(1, bot.frames.size)
    }

    @Test
    fun `Selection adapts only this bot and retains a minimum base goal`() {
        val manager = BotManager(mutableMapOf(parent.id to parent), world = FakeWorld())
        val bot = testBot()
        val other = testBot()
        manager.tick(bot)
        val selected = bot.activity as BotActivity
        assertNotSame(parent, selected)
        assertEquals(listOf(goal), parent.requires)
        assertTrue(parent.setup.isEmpty())
        assertTrue(selected.requires.isEmpty())
        assertEquals(listOf(goal.copy(max = null)), selected.setup)
        assertFalse(parent.requires.all { it.check(other.player) })
        assertNull(manager.adaptSkillGoal(bot, parent.copy(requires = listOf(goal.copy(current = true)))))
        assertNull(manager.adaptSkillGoal(bot, parent.copy(requires = listOf(goal, BotSkillLevel(Skill.Mining, min = 10)))))
    }

    @Test
    fun `Selection chance is made only when choosing an activity`() {
        var rolls = 0
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int {
                rolls++
                return until - 1
            }
        })
        val manager = BotManager(mutableMapOf("yews" to parent), world = FakeWorld())
        val bot = testBot()
        manager.tick(bot)
        assertEquals("idle", bot.activity!!.id)
        repeat(5) { manager.tick(bot) }
        assertEquals(1, rolls)
    }

    @Test
    fun `Configured training chance supports disabling enabling and percentage boundaries`() {
        val oldChance = Settings["bots.trainingGoalChance", 10]
        val oldSpawnRequirements = Settings["bots.spawnRequirements", false]
        Settings.load(mapOf("bots.spawnRequirements" to "false"))
        try {
            for ((chance, roll, selected) in listOf(
                Triple(0, 0, false),
                Triple(100, 99, true),
                Triple(10, 9, true),
                Triple(10, 10, false),
                Triple(-1, 0, false),
                Triple(101, 99, true),
            )) {
                Settings.load(mapOf("bots.trainingGoalChance" to chance.toString()))
                setRandom(object : FakeRandom() {
                    override fun nextInt(until: Int) = if (until == 100) roll else 0
                })
                val manager = BotManager(mutableMapOf(parent.id to parent), world = FakeWorld())
                val bot = testBot()
                manager.tick(bot)
                assertEquals(if (selected) parent.id else "idle", bot.activity!!.id, "chance=$chance roll=$roll")
            }
        } finally {
            Settings.load(mapOf("bots.trainingGoalChance" to oldChance.toString(), "bots.spawnRequirements" to oldSpawnRequirements.toString()))
        }
    }

    @Test
    fun `Boosted current level does not meet the base level goal`() {
        val trees = batch()
        val manager = BotManager(mutableMapOf("yews" to parent, "trees" to trees), world = FakeWorld())
        val bot = testBot()
        bot.player.levels.set(skill, 99)
        manager.tick(bot)
        startBatch(manager, bot)
        assertEquals("trees", bot.frame().behaviour.id)
    }

    @Test
    fun `Training child uses ordinary setup resolvers and preserves parent during preparation`() {
        val kit = BotVariable("has_kit", true, false)
        val trees = batch().copy(setup = listOf(kit))
        val resolver = Resolver("fetch_kit", 1, actions = listOf(BotWait(1)))
        val manager = BotManager(
            mutableMapOf("yews" to parent, "trees" to trees),
            mutableMapOf("var:has_kit" to mutableListOf(resolver)),
            world = FakeWorld(),
        )
        val bot = testBot()
        manager.tick(bot)
        val selected = bot.previous
        startBatch(manager, bot)
        manager.tick(bot)
        assertEquals(4, bot.frames.size)
        assertEquals(resolver, bot.frame().behaviour)
        assertSame(selected, bot.previous)
        bot.player["has_kit"] = true
        bot.frame().state = BehaviourState.Success
        manager.tick(bot)
        manager.tick(bot)
        assertEquals("trees", bot.frame().behaviour.id)
        assertEquals(BehaviourState.Running, bot.frame().state)
    }

    @Test
    fun `Slot taken after resolver selection prevents nested assignment`() {
        setArea()
        val trees = batch(area = "trees")
        val manager = BotManager(mutableMapOf("yews" to parent, "trees" to trees), world = FakeWorld())
        val bot = testBot()
        manager.tick(bot)
        manager.tick(bot)
        val worker = testBot()
        manager.assign(worker, trees)
        manager.tick(bot)
        assertEquals(2, bot.frames.size)
        assertEquals(BehaviourState.Failed(Reason.NoTarget), bot.frame().state)
        manager.stop(bot)
        assertFalse(manager.slots.hasFree(trees))
        manager.stop(worker)
        assertTrue(manager.slots.hasFree(trees))
    }

    @Test
    fun `Soft failed batch returns through resolver and selects another eligible activity`() {
        val trees = batch()
        val otherTrees = batch("other_trees")
        val manager = BotManager(mutableMapOf("yews" to parent, "trees" to trees, "other_trees" to otherTrees), world = FakeWorld())
        val bot = testBot()
        manager.tick(bot)
        val selected = bot.previous
        startBatch(manager, bot)
        bot.frame().fail(Reason.NoTarget)
        manager.tick(bot)
        manager.tick(bot)
        manager.tick(bot)
        startBatch(manager, bot)
        assertEquals("other_trees", bot.frame().behaviour.id)
        assertSame(selected, bot.previous)
    }

    @Test
    fun `Ordinary resolver inside training cannot recursively plan an unmet skill`() {
        val kit = BotVariable("has_kit", true, false)
        val trees = batch().copy(setup = listOf(kit))
        val resolver = Resolver("fetch_kit", 1, setup = listOf(goal), actions = listOf(BotWait(1)))
        val manager = BotManager(
            mutableMapOf("yews" to parent, "trees" to trees),
            mutableMapOf("var:has_kit" to mutableListOf(resolver)),
            world = FakeWorld(),
        )
        val bot = testBot()
        manager.tick(bot)
        startBatch(manager, bot)
        manager.tick(bot)
        manager.tick(bot)
        assertEquals(4, bot.frames.size)
        assertEquals(BehaviourState.Failed(Reason.Requirement(goal)), bot.frame().state)
    }

    @Test
    fun `Completed batches retry and switch to newly eligible training without replacing parent`() {
        val trees = batch()
        val oaks = batch("oaks", 15, 30)
        val manager = BotManager(mutableMapOf("yews" to parent, "trees" to trees, "oaks" to oaks), world = FakeWorld())
        val bot = testBot()
        manager.tick(bot)
        val selected = bot.previous
        startBatch(manager, bot)
        assertEquals("trees", bot.frame().behaviour.id)
        assertSame(selected, bot.previous)
        finishBatch(manager, bot) // No progress: another finite batch is allowed.
        startBatch(manager, bot)
        assertEquals("trees", bot.frame().behaviour.id)
        level(bot, 16)
        finishBatch(manager, bot)
        startBatch(manager, bot)
        assertEquals("oaks", bot.frame().behaviour.id)
        level(bot, 76) // Overshoot within the batch still meets the minimum goal.
        finishBatch(manager, bot)
        manager.tick(bot)
        assertTrue(bot.frame().state is BehaviourState.Wait)
        assertSame(selected, bot.previous)
        assertSame(selected, bot.activity)
    }

    @Test
    fun `Satisfied setup needs no training and unavailable skill goal fails without grants`() {
        val activity = parent.copy(requires = emptyList(), setup = listOf(goal.copy(max = null)))
        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bot = testBot()
        manager.assign(bot, activity)
        manager.tick(bot)
        assertEquals(BehaviourState.Failed(Reason.Requirement(activity.setup.single())), bot.frame().state)
        assertEquals(1, bot.player.levels.getMax(skill))
        manager.tick(bot)
        assertTrue(bot.noTask())
        level(bot, 60)
        manager.assign(bot, activity)
        manager.tick(bot)
        assertEquals(1, bot.frames.size)
        assertTrue(bot.frame().state is BehaviourState.Wait)
    }

    @Test
    fun `Candidates must pass requirements setup skills capacity blocking and finite batch checks`() {
        setArea()
        val trees = batch(area = "trees")
        val manager = BotManager(mutableMapOf("trees" to trees), world = FakeWorld())
        val bot = testBot()
        assertEquals(trees, manager.trainingActivity(bot, goal))
        bot.blocked.add("trees")
        assertNull(manager.trainingActivity(bot, goal))
        bot.blocked.clear()
        val worker = testBot()
        manager.assign(worker, trees)
        assertNull(manager.trainingActivity(bot, goal))
        manager.stop(worker)
        assertEquals(trees, manager.trainingActivity(bot, goal))
        for (invalid in listOf(
            trees.copy(requires = listOf(goal)),
            trees.copy(setup = listOf(goal)),
            trees.copy(actions = trees.actions + BotRestart(emptyList(), BotVariable("loop", false, true))),
            trees.copy(produces = setOf("skill:mining")),
        )) {
            val filtered = BotManager(mutableMapOf("trees" to invalid), world = FakeWorld())
            assertNull(filtered.trainingActivity(bot, goal))
        }
    }

    @Test
    fun `Inventory bounded npc gathering and processing restart loops are finite candidates`() {
        val fishing = batch().copy(actions = listOf(BotInteractNpc("Net", "fishing_spot", success = full)))
        val processing = batch().copy(actions = listOf(BotWait(1), BotRestart(emptyList(), full)))
        assertTrue(TrainingResolver.finite(fishing))
        assertTrue(TrainingResolver.finite(processing))
    }

    @Test
    fun `Nested batch completion failure and stop release existing area slots`() {
        setArea()
        val trees = batch(area = "trees")
        for (failure in listOf(null, Reason.NoTarget, Reason.Timeout, Reason.Cancelled)) {
            val manager = BotManager(mutableMapOf("yews" to parent, "trees" to trees), world = FakeWorld())
            val bot = testBot()
            manager.tick(bot)
            val selected = bot.previous
            startBatch(manager, bot)
            assertFalse(manager.slots.hasFree(trees))
            if (failure == null) {
                finishBatch(manager, bot)
            } else {
                bot.frame().fail(failure)
                manager.tick(bot)
            }
            assertTrue(manager.slots.hasFree(trees))
            assertSame(selected, bot.previous)
            manager.stop(bot)
            assertTrue(manager.slots.hasFree(trees))
        }
    }

    @Test
    fun `Spawn requirements fallback cannot grant a failed training goal after idling`() {
        val old = Settings["bots.spawnRequirements", false]
        Settings.load(mapOf("bots.spawnRequirements" to "true"))
        try {
            val manager = BotManager(mutableMapOf("yews" to parent), world = FakeWorld())
            val bot = testBot()
            manager.tick(bot)
            val selected = bot.previous
            bot.blocked.add("yews")
            manager.stop(bot)
            repeat(2) {
                manager.tick(bot)
                assertEquals("idle", bot.activity!!.id)
                assertSame(selected, bot.previous)
                assertEquals(1, bot.player.levels.getMax(skill))
                manager.stop(bot)
            }
        } finally {
            Settings.load(mapOf("bots.spawnRequirements" to old.toString()))
        }
    }
}
