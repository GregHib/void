package content.bot

import FakeRandom
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.SoftReason
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.action.BotWait
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotAtTile
import content.bot.behaviour.condition.BotSkillLevel
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.setup.Resolver
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import content.bot.behaviour.condition.BotInArea
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.type.setRandom

class BotManagerTest {

    fun testBot(vararg activities: BotActivity, name: String = "bot") = Bot(Player(accountName = name)).also {
        // Real players link levels at login; mirror that here so BotSkillLevel.check (which reads
        // levels.getMax) doesn't throw on the lateinit `level` property.
        it.player.experience.player = it.player
        it.player.levels.link(it.player, PlayerLevels(it.player.experience))
        it.available.addAll(activities.map { a -> a.id })
    }

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
    }

    @Test
    fun `Taskless bot gets assigned an activity`() {
        val activity = testActivity(
            id = "woodcutting",
            plan = listOf(BotWait(1)),
        )
        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bot = testBot(activity)

        manager.tick(bot)

        assertEquals(1, bot.frames.size)
        assertEquals(activity, bot.previous)
    }

    @Test
    fun `Area capacity is respected`() {
        setAreas("mine_area" to 1)
        val activity = testActivity(
            id = "mine",
            plan = listOf(BotWait(1)),
            setup = listOf(BotInArea("mine_area")),
        )
        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())

        val bot1 = testBot(activity, name = "bot1")
        val bot2 = testBot(activity, name = "bot2")

        manager.tick(bot1)
        manager.tick(bot2)

        assertEquals(1, bot1.frames.size)
        assertEquals("idle", bot2.frames.first().behaviour.id)
    }

    @Test
    fun `Hard failure releases only the failed bots activity slot`() {
        setAreas("mine_area" to 2)
        val activity = testActivity(id = "mine", plan = listOf(BotWait(1)), setup = listOf(BotInArea("mine_area")))
        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val failedBot = testBot(activity, name = "failed")
        val survivingBot = testBot(activity, name = "surviving")
        val replacementBot = testBot(activity, name = "replacement")
        val extraBot = testBot(activity, name = "extra")

        manager.tick(failedBot)
        manager.tick(survivingBot)
        assertFalse(manager.slots.hasFree(activity))

        failedBot.frame().fail(Reason.Timeout)
        manager.tick(failedBot)
        assertTrue(failedBot.noTask())
        assertEquals("mine", survivingBot.activity!!.id)

        manager.tick(replacementBot)
        assertEquals("mine", replacementBot.activity!!.id)
        manager.tick(extraBot)

        assertEquals("idle", extraBot.activity!!.id, "The surviving bot and replacement should occupy both mining slots")
    }

    @Test
    fun `Idle bot takes available work after its wait completes`() {
        setAreas("mine_area" to 1)
        val activity = testActivity(id = "mine", plan = listOf(BotWait(1)), setup = listOf(BotInArea("mine_area")))
        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val worker = testBot(activity, name = "worker")
        val waitingBot = testBot(activity, name = "waiting")

        manager.tick(worker)
        manager.tick(waitingBot)
        assertEquals("idle", waitingBot.activity!!.id)

        manager.tick(waitingBot)
        val waitTicks = (waitingBot.frame().state as BehaviourState.Wait).ticks
        manager.stop(worker)
        assertTrue(manager.slots.hasFree(activity))

        repeat(waitTicks) { manager.tick(waitingBot) }
        manager.tick(waitingBot) // Complete idle and release its slot.
        assertTrue(waitingBot.noTask())
        manager.tick(waitingBot) // Choose the next activity with mining now available.

        assertEquals("mine", waitingBot.activity!!.id, "Bot should leave idle when the mining slot becomes available")
    }

    @Test
    fun `Pending frame starts running`() {
        val activity = testActivity(
            id = "walk",
            plan = listOf(BotWait(1, BehaviourState.Running)),
        )
        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bot = testBot(activity)

        manager.tick(bot)
        manager.tick(bot)
        manager.tick(bot)

        assertEquals(BehaviourState.Running, bot.frame().state)
    }

    @Test
    fun `Success advances frame index`() {
        val activity = testActivity(
            id = "task",
            plan = listOf(
                BotWait(1),
                BotWait(1),
            ),
        )
        val frame = BehaviourFrame(activity)
        frame.start(testBot(activity), FakeWorld())
        frame.success()

        val advanced = frame.next()

        assertTrue(advanced)
        assertEquals(1, frame.index)
        assertEquals(BehaviourState.Running, frame.state)
    }

    @Test
    fun `Activity slot released on success`() {
        val activity = testActivity(
            id = "cook",
            plan = listOf(BotWait(1)),
        )

        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bot = testBot(activity)

        manager.tick(bot)
        manager.tick(bot)

        bot.frame().success()
        manager.tick(bot)
        assertTrue(manager.slots.hasFree(activity))
        assertEquals(0, bot.frames.size)
    }

    @Test
    fun `Completed activity most likely to be reassigned on success`() {
        setAreas("cook_area" to 1)
        val activity = testActivity(
            id = "cook",
            plan = listOf(BotWait(1)),
            setup = listOf(BotInArea("cook_area")),
        )
        val test = testActivity(
            id = "test",
            plan = listOf(BotWait(1)),
        )

        val activities = mutableMapOf(activity.id to activity, test.id to test)
        val manager = BotManager(activities, world = FakeWorld())
        val bot = testBot(activity, test)

        bot.previous = activity

        manager.tick(bot)

        assertFalse(manager.slots.hasFree(activity))
        assertEquals(activity.bind("cook_area"), bot.previous)
        assertEquals(activity.bind("cook_area"), bot.frames.peek().behaviour)
        assertEquals(1, bot.frames.size)
    }

    @Test
    fun `Activity slot released on failure`() {
        val activity = testActivity(
            id = "smith",
            plan = listOf(BotWait(1)),
        )

        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bot = testBot(activity)

        manager.tick(bot)
        manager.tick(bot)

        bot.frame().fail(Reason.Cancelled)
        manager.tick(bot)

        assertTrue(bot.frames.isEmpty())
    }

    @Test
    fun `Failed activity is blocked`() {
        val activity = testActivity(
            id = "fish",
            plan = listOf(BotWait(1)),
        )

        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bot = testBot(activity)

        manager.tick(bot)
        manager.tick(bot)
        bot.frame().fail(Reason.Requirement(BotAtTile()))
        manager.tick(bot)
        manager.tick(bot)

        assertEquals("idle", bot.frames.first().behaviour.id)
        assertTrue("fish" in bot.blocked)
    }

    @Test
    fun `Behaviour without requirements isn't started`() {
        val activity = testActivity(
            id = "test",
            requires = listOf(
                BotSkillLevel(Skill.Attack, 99),
            ),
            plan = listOf(BotWait(4)),
        )

        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bot = testBot(activity)
        bot.frames.add(BehaviourFrame(activity))

        manager.tick(bot)
        assertTrue(bot.frame().state is BehaviourState.Failed)
        manager.tick(bot)

        assertTrue(bot.frames.isEmpty())
        assertTrue("test" in bot.blocked)
    }

    @Test
    fun `Resolvable requirement queues resolver before activity starts`() {
        val condition = BotAtTile(100, 100, 2)
        val resolver = Resolver(
            id = "go_to_area",
            weight = 1,
            actions = listOf(BotWait(1)),
            produces = setOf("tile"),
        )
        val activity = testActivity(
            id = "woodcut",
            resolves = listOf(condition),
            plan = listOf(BotWait(1)),
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver)),
            world = FakeWorld(),
        )

        val bot = testBot(activity)
        manager.tick(bot)
        manager.tick(bot)

        assertEquals(2, bot.frames.size)
        assertEquals(resolver, bot.frames.peek().behaviour)
    }

    @Test
    fun `Lowest weight resolver is selected`() {
        val condition = BotAtTile(100, 100, 2)

        val bad = Resolver("bad", weight = 10, actions = listOf(BotWait(1)))
        val good = Resolver("good", weight = 1, actions = listOf(BotWait(1)))

        val activity = testActivity(
            id = "mine",
            resolves = listOf(condition),
            plan = listOf(BotWait(1)),
        )

        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(bad, good)),
            world = FakeWorld(),
        )

        val bot = testBot(activity)
        manager.tick(bot)
        manager.tick(bot)

        assertEquals("good", bot.frames.peek().behaviour.id)
    }

    @Test
    fun `Blocked resolver is not reselected`() {
        val condition = BotAtTile(100, 100, 2)
        val resolver = Resolver(id = "get_key", weight = 1, actions = listOf(BotWait(1)))
        val activity = testActivity(
            id = "open_door",
            resolves = listOf(condition),
            plan = listOf(BotWait(1)),
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver)),
            world = FakeWorld(),
        )

        val bot = testBot(activity)
        manager.tick(bot)
        assertEquals(1, bot.frames.size)
        val frame = bot.frames.last()
        frame.blocked.add("get_key")
        manager.tick(bot)
        manager.tick(bot)

        assertTrue(bot.frames.isEmpty())
        assertTrue("open_door" in bot.blocked)
    }

    @Test
    fun `Hard failure in resolver stops bot`() {
        val condition = BotAtTile(100, 100, 2)
        val resolver = Resolver(
            id = "walk",
            weight = 1,
            actions = listOf(BotWait(1)),
        )
        val activity = testActivity(
            id = "enter_zone",
            resolves = listOf(condition),
            plan = listOf(BotWait(1)),
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver)),
            world = FakeWorld(),
        )

        val bot = testBot(activity)
        manager.tick(bot)
        manager.tick(bot)
        assertEquals(2, bot.frames.size)
        bot.frame().fail(Reason.Cancelled)
        manager.tick(bot)

        assertTrue(bot.frames.isEmpty())
    }

    @Test
    fun `Soft failure in resolver only pops resolver`() {
        val condition = BotAtTile(100, 100, 2)
        val resolver = Resolver(
            id = "test",
            weight = 1,
            actions = listOf(BotWait(1)),
        )
        val activity = testActivity(
            id = "smelt",
            resolves = listOf(condition),
            plan = listOf(BotWait(1)),
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver)),
            world = FakeWorld(),
        )

        val bot = testBot(activity)
        bot.player["debug"] = true
        manager.tick(bot)
        manager.tick(bot)
        assertEquals(2, bot.frames.size)
        bot.frame().fail(object : SoftReason {})
        manager.tick(bot)

        assertEquals(activity, bot.frame().behaviour)
    }

    @Test
    fun `Resolver with unmet mandatory requirements is skipped`() {
        val condition = BotAtTile(100, 100, 2)
        val resolver = Resolver(
            id = "mine_gem",
            weight = 1,
            actions = listOf(BotWait(1)),
            requires = listOf(BotSkillLevel(Skill.Mining, 99)),
        )
        val activity = testActivity(
            id = "craft",
            resolves = listOf(condition),
            plan = listOf(BotWait(1)),
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver)),
            world = FakeWorld(),
        )

        val bot = testBot(activity)
        manager.tick(bot)
        manager.tick(bot)
        manager.tick(bot)

        assertTrue(bot.frames.isEmpty())
        assertTrue("craft" in bot.blocked)
    }

    @Test
    fun `Activity are occupied while resolver is running`() {
        val condition = BotAtTile(100, 100, 2)
        val resolver = Resolver(
            id = "get_tool",
            weight = 1,
            actions = listOf(BotWait(1)),
        )
        setAreas("work_area" to 1)
        val activity = testActivity(
            id = "work",
            resolves = listOf(condition),
            setup = listOf(BotInArea("work_area")),
            plan = listOf(BotWait(1)),
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver)),
            world = FakeWorld(),
        )

        val bot = testBot(activity)
        manager.tick(bot)
        manager.tick(bot)

        assertFalse(manager.slots.hasFree(activity))
    }

    private fun setAreas(vararg areas: Pair<String, Int?>, tags: Map<String, Set<String>> = emptyMap()) {
        val definitions = areas.associate { (name, capacity) ->
            name to AreaDefinition(
                name = name,
                area = Rectangle(0, 0, 10, 10),
                tags = tags[name] ?: emptySet(),
                params = capacity?.let { mapOf(Params.BOT_CAPACITY to it) },
            )
        }
        val tagged = mutableMapOf<String, MutableSet<AreaDefinition>>()
        for ((name, set) in tags) {
            for (tag in set) {
                tagged.getOrPut(tag) { mutableSetOf() }.add(definitions.getValue(name))
            }
        }
        Areas.set(definitions, tagged)
    }

    @Test
    fun `Activity with multiple areas is bound to one with free slots`() {
        setAreas("field_a" to 1, "field_b" to 1)
        val activity = testActivity("cows", plan = listOf(BotWait(1)), setup = listOf(BotInArea(listOf("field_a", "field_b"))))
        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bots = List(3) { testBot(activity, name = "bot$it") }

        bots.forEach { manager.tick(it) }

        val areas = bots.take(2).map { (it.activity as BotActivity).boundArea }
        assertEquals(setOf("field_a", "field_b"), areas.toSet())
        assertEquals("idle", bots[2].activity!!.id, "Both fields are full")
        assertEquals(BotInArea(areas[0]!!), (bots[0].activity as BotActivity).area)
    }

    @Test
    fun `Tagged areas are included`() {
        setAreas("field_a" to 1, "field_b" to 1, "field_c" to 1, tags = mapOf("field_b" to setOf("cow_field"), "field_c" to setOf("cow_field")))
        val activity = testActivity("cows", plan = listOf(BotWait(1)), setup = listOf(BotInArea(listOf("field_a"), setOf("cow_field"))))
        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bots = List(4) { testBot(activity, name = "bot$it") }

        bots.forEach { manager.tick(it) }

        assertEquals(setOf("field_a", "field_b", "field_c"), bots.take(3).map { (it.activity as BotActivity).boundArea }.toSet())
        assertEquals("idle", bots[3].activity!!.id)
    }

    @Test
    fun `Repeated activity stays in the previous area`() {
        setAreas("field_a" to 1, "field_b" to 1)
        val activity = testActivity("cows", plan = listOf(BotWait(1)), setup = listOf(BotInArea(listOf("field_a", "field_b"))))
        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bot = testBot(activity)
        manager.tick(bot)
        val first = (bot.activity as BotActivity).boundArea
        manager.stop(bot)
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })

        manager.tick(bot)

        assertEquals(first, (bot.activity as BotActivity).boundArea)
    }

    @Test
    fun `Closer areas are more likely`() {
        Areas.set(
            mapOf(
                "near" to AreaDefinition("near", Rectangle(0, 0, 4, 4)),
                "far" to AreaDefinition("far", Rectangle(3000, 3000, 3004, 3004)),
            ),
        )
        val activity = testActivity("cows", plan = listOf(BotWait(1)), setup = listOf(BotInArea(listOf("far", "near"))))
        val manager = BotManager(mutableMapOf(activity.id to activity), world = FakeWorld())
        val bot = testBot(activity)
        // A mid roll would land on 'far' if weights were equal, but 'near' is much heavier
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until / 2
        })

        assertEquals("near", manager.bind(bot, activity).boundArea)
    }

    @Test
    fun `Unknown area or empty tag fails validation`() {
        setAreas("field_a" to 1)
        val missing = testActivity("a", plan = listOf(BotWait(1)), setup = listOf(BotInArea(listOf("field_a", "nope"))))
        assertThrows(IllegalArgumentException::class.java) { BotManager(mutableMapOf(missing.id to missing), world = FakeWorld()).validateAreas() }
        val tag = testActivity("b", plan = listOf(BotWait(1)), setup = listOf(BotInArea(emptyList(), setOf("no_tag"))))
        assertThrows(IllegalArgumentException::class.java) { BotManager(mutableMapOf(tag.id to tag), world = FakeWorld()).validateAreas() }
    }

    fun testActivity(
        id: String,
        requires: List<Condition> = emptyList(),
        resolves: List<Condition> = emptyList(),
        plan: List<BotAction>,
        setup: List<Condition> = emptyList(),
    ) = BotActivity(id = id, timeout = 50, requires = requires, setup = resolves + setup, actions = plan)
}
