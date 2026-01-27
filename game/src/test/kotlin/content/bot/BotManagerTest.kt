package content.bot

import content.bot.action.BehaviourFrame
import content.bot.action.BehaviourState
import content.bot.action.BotAction
import content.bot.action.BotActivity
import content.bot.action.Reason
import content.bot.fact.MandatoryFact
import content.bot.fact.HasSkillLevel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player

class BotManagerTest {

    fun testBot(name: String = "bot") = Bot(Player(accountName = name))

    @Test
    fun `Idle bot gets assigned an activity`() {
        val activity = testActivity(
            id = "woodcutting",
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(mapOf(activity.id to activity))
        val bot = testBot()

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
        val manager = BotManager(mapOf(activity.id to activity))

        val bot1 = testBot("bot1")
        val bot2 = testBot("bot2")

        manager.tick(bot1)
        manager.tick(bot2)

        assertEquals(1, bot1.frames.size)
        assertTrue(bot2.frames.isEmpty())
    }

    @Test
    fun `Pending frame starts running`() {
        val activity = testActivity(
            id = "walk",
            plan = listOf(BotAction.Wait(1))
        )
        val manager = BotManager(mapOf(activity.id to activity))
        val bot = testBot()

        manager.tick(bot)
        manager.tick(bot)

        assertEquals(BehaviourState.Running, bot.frame().state)
    }

    @Test
    fun `Success advances frame index`() {
        val activity = testActivity(
            id = "task",
            plan = listOf(
                BotAction.Wait(1),
                BotAction.Wait(1)
            )
        )
        val frame = BehaviourFrame(activity)
        frame.start(testBot())
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

        val manager = BotManager(mapOf(activity.id to activity))
        val bot = testBot()

        manager.tick(bot)
        manager.tick(bot)

        val frame = bot.frame()
        repeat(3) {
            frame.fail(Reason.Requirements)
            manager.tick(bot)
            assertTrue(frame.state is BehaviourState.Wait)
            manager.tick(bot) // Tick 1
            manager.tick(bot) // Tick 2
            manager.tick(bot) // Pending
        }

        // after retries exhausted → popped
        assertTrue(bot.frames.isEmpty())
        assertTrue("talk" in bot.blocked)
    }

    @Test
    fun `Activity slot released on success`() {
        val activity = testActivity(
            id = "cook",
            plan = listOf(BotAction.Wait(1))
        )

        val manager = BotManager(mapOf(activity.id to activity))
        val bot = testBot()

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
        val bot = testBot()

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

        val manager = BotManager(mapOf(activity.id to activity))
        val bot = testBot()

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
            plan = listOf(BotAction.Wait(1))
        )

        val manager = BotManager(mapOf(activity.id to activity))
        val bot = testBot()

        manager.tick(bot)
        manager.tick(bot)
        bot.frame().fail(Reason.Requirements)
        manager.tick(bot)
        manager.tick(bot)

        assertTrue(bot.frames.isEmpty())
        assertTrue("fish" in bot.blocked)
    }

    @Test
    fun `Behaviour without requirements isn't started`() {
        val activity = testActivity(
            id = "test",
            requirements = listOf(
                HasSkillLevel("attack", 99, 99)
            ),
            plan = listOf(BotAction.Wait(4))
        )

        val manager = BotManager(mapOf(activity.id to activity))
        val bot = testBot()
        bot.frames.add(BehaviourFrame(activity))

        manager.tick(bot)
        assertTrue(bot.frame().state is BehaviourState.Failed)
        manager.tick(bot)

        assertTrue(bot.frames.isEmpty())
        assertTrue("test" in bot.blocked)
    }

    fun testActivity(
        id: String,
        requirements: List<MandatoryFact> = emptyList(),
        plan: List<BotAction>
    ) = BotActivity(id, 1, requirements, plan)
}