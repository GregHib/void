package content.bot

import content.bot.action.*
import content.bot.fact.Condition
import content.bot.fact.Fact
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class BotManagerTest {

    fun testBot(vararg activities: BotActivity, name: String = "bot") = Bot(Player(accountName = name)).also { it.available.addAll(activities.map { a -> a.id }) }

    @Test
    fun `Taskless bot gets assigned an activity`() {
        val activity = testActivity(
            id = "woodcutting",
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(mutableMapOf(activity.id to activity))
        val bot = testBot(activity)

        manager.tick(bot)

        assertEquals(1, bot.frames.size)
        assertEquals(activity, bot.previous)
    }

    @Test
    fun `Activity capacity is respected`() {
        val activity = testActivity(
            id = "mine",
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(mutableMapOf(activity.id to activity))

        val bot1 = testBot(activity, name = "bot1")
        val bot2 = testBot(activity, name = "bot2")

        manager.tick(bot1)
        manager.tick(bot2)

        assertEquals(1, bot1.frames.size)
        assertEquals("idle", bot2.frames.first().behaviour.id)
    }

    @Test
    fun `Pending frame starts running`() {
        val activity = testActivity(
            id = "walk",
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(mutableMapOf(activity.id to activity))
        val bot = testBot(activity)

        manager.tick(bot)
        manager.tick(bot)

        assertEquals(BehaviourState.Running, bot.frame().state)
    }

    @Test
    fun `Success advances frame index`() {
        val activity = testActivity(
            id = "task",
            plan = listOf(
                BotAction.Clone("1"),
                BotAction.Clone("1")
            )
        )
        val frame = BehaviourFrame(activity)
        frame.start(testBot(activity))
        frame.success()


        val advanced = frame.next()


        assertTrue(advanced)
        assertEquals(1, frame.index)
        assertEquals(BehaviourState.Pending, frame.state)
    }

    @Test
    fun `Retryable action retries before failing`() {
        val action = BotAction.InteractNpc(
            option = "Talk",
            id = "npc",
            retryTicks = 2,
            retryMax = 2
        )

        val activity = testActivity(
            id = "talk",
            plan = listOf(action)
        )

        val manager = BotManager(mutableMapOf(activity.id to activity))
        val bot = testBot(activity)

        manager.tick(bot)
        manager.tick(bot)

        val frame = bot.frame()
        repeat(3) {
            frame.fail(Reason.Requirement(Condition.Clone("")))
            manager.tick(bot)
            assertTrue(frame.state is BehaviourState.Wait)
            manager.tick(bot) // Tick 1
            manager.tick(bot) // Tick 2
            manager.tick(bot) // Pending
        }

        // after retries exhausted → popped
        assertEquals("idle", bot.frames.first().behaviour.id)
        assertTrue("talk" in bot.blocked)
    }

    @Test
    fun `Activity slot released on success`() {
        val activity = testActivity(
            id = "cook",
            plan = listOf(BotAction.Wait(1))
        )

        val manager = BotManager(mutableMapOf(activity.id to activity))
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
        val activity = testActivity(
            id = "cook",
            plan = listOf(BotAction.Wait(1))
        )
        val test = testActivity(
            id = "test",
            plan = listOf(BotAction.Wait(1))
        )

        val activities = mutableMapOf(activity.id to activity, test.id to test)
        val manager = BotManager(activities)
        val bot = testBot(activity, test)

        bot.previous = activity

        manager.tick(bot)

        assertFalse(manager.slots.hasFree(activity))
        assertEquals(activity, bot.previous)
        assertEquals(activity, bot.frames.peek().behaviour)
        assertEquals(1, bot.frames.size)
    }

    @Test
    fun `Activity slot released on failure`() {
        val activity = testActivity(
            id = "smith",
            plan = listOf(BotAction.Wait(1))
        )

        val manager = BotManager(mutableMapOf(activity.id to activity))
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
            plan = listOf(BotAction.Clone(""))
        )

        val manager = BotManager(mutableMapOf(activity.id to activity))
        val bot = testBot(activity)

        manager.tick(bot)
        manager.tick(bot)
        bot.frame().fail(Reason.Requirement(Condition.Clone("")))
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
                Condition.Range(Fact.AttackLevel, 99, 99)
            ),
            plan = listOf(BotAction.Wait(4))
        )

        val manager = BotManager(mutableMapOf(activity.id to activity))
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
        val condition = Condition.Within(Fact.PlayerTile, Tile(100, 100, 2), 2)
        val resolver = Resolver(
            id = "go_to_area",
            weight = 1,
            actions = listOf(BotAction.Wait(1)),
            produces = setOf(condition)
        )
        val activity = testActivity(
            id = "woodcut",
            resolves = listOf(condition),
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver))
        )

        val bot = testBot(activity)
        manager.tick(bot)
        manager.tick(bot)

        assertEquals(2, bot.frames.size)
        assertEquals(resolver, bot.frames.peek().behaviour)
    }

    @Test
    fun `Lowest weight resolver is selected`() {
        val condition = Condition.Within(Fact.PlayerTile, Tile(100, 100, 2), 2)

        val bad = Resolver("bad", weight = 10, actions = listOf(BotAction.Clone("")))
        val good = Resolver("good", weight = 1, actions = listOf(BotAction.Clone("")))

        val activity = testActivity(
            id = "mine",
            resolves = listOf(condition),
            plan = listOf(BotAction.Clone(""))
        )

        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(bad, good))
        )

        val bot = testBot(activity)
        manager.tick(bot)
        manager.tick(bot)

        assertEquals("good", bot.frames.peek().behaviour.id)
    }

    @Test
    fun `Blocked resolver is not reselected`() {
        val condition = Condition.Within(Fact.PlayerTile, Tile(100, 100, 2), 2)
        val resolver = Resolver(id = "get_key", weight = 1, actions = listOf(BotAction.Clone("")))
        val activity = testActivity(
            id = "open_door",
            resolves = listOf(condition),
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver))
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
        val condition = Condition.Within(Fact.PlayerTile, Tile(100, 100, 2), 2)
        val resolver = Resolver(
            id = "walk",
            weight = 1,
            actions = listOf(BotAction.Wait(1))
        )
        val activity = testActivity(
            id = "enter_zone",
            resolves = listOf(condition),
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver))
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
        val condition = Condition.Within(Fact.PlayerTile, Tile(100, 100, 2), 2)
        val resolver = Resolver(
            id = "test",
            weight = 1,
            actions = listOf(BotAction.Wait(1))
        )
        val activity = testActivity(
            id = "smelt",
            resolves = listOf(condition),
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver))
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
        val condition = Condition.Within(Fact.PlayerTile, Tile(100, 100, 2), 2)
        val resolver = Resolver(
            id = "mine_gem",
            weight = 1,
            actions = listOf(BotAction.Clone("")),
            requires = listOf(Condition.Range(Fact.MiningLevel, 99, 99))
        )
        val activity = testActivity(
            id = "craft",
            resolves = listOf(condition),
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver))
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
        val condition = Condition.Within(Fact.PlayerTile, Tile(100, 100, 2), 2)
        val resolver = Resolver(
            id = "get_tool",
            weight = 1,
            actions = listOf(BotAction.Wait(1))
        )
        val activity = testActivity(
            id = "work",
            resolves = listOf(condition),
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(
            mutableMapOf(activity.id to activity),
            mutableMapOf(condition.keys().first() to mutableListOf(resolver))
        )

        val bot = testBot(activity)
        manager.tick(bot)
        manager.tick(bot)

        assertFalse(manager.slots.hasFree(activity))
    }

    fun testActivity(
        id: String,
        requires: List<Condition> = emptyList(),
        resolves: List<Condition> = emptyList(),
        plan: List<BotAction>,
    ) = BotActivity(id, 1, requires, resolves, plan)
}