package content.bot

import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.action.BotRestart
import content.bot.behaviour.action.BotWait
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotInArea
import content.bot.behaviour.condition.BotVariable
import content.bot.behaviour.setup.Resolver
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

class BehaviourCompletionTest {
    private val helper = BotManagerTest()
    private val goal = BotVariable("finished", true, false)
    private val parent = BotActivity("parent", actions = listOf(BotWait(1)))
    private val child = BotActivity("child", actions = listOf(BotWait(100), BotRestart(emptyList(), BotVariable("forever", true, false))))

    @Test
    fun `Completion skips remaining actions in pending running waiting and successful frames`() {
        for (state in listOf(BehaviourState.Pending, BehaviourState.Running, BehaviourState.Wait(100, BehaviourState.Running), BehaviourState.Success)) {
            val bot = helper.testBot()
            val manager = BotManager(world = FakeWorld())
            manager.assign(bot, parent)
            manager.assign(bot, child, nested = true, completionCondition = goal)
            val frame = bot.frame()
            frame.state = state
            bot.player["finished"] = true

            manager.tick(bot)

            assertEquals(1, bot.frames.size, "Did not complete from $state")
            assertEquals(BehaviourState.Success, frame.state)
            assertEquals(0, frame.index, "A remaining action was started")
            assertSame(parent, bot.previous)
            assertFalse(bot.blocked.contains(child.id))
        }
    }

    @Test
    fun `Completion condition belongs to one execution and leaves ordinary frames unchanged`() {
        val manager = BotManager(world = FakeWorld())
        val bot = helper.testBot()
        val other = helper.testBot()
        manager.assign(bot, child, completionCondition = goal)
        manager.assign(other, child)
        bot.player["finished"] = true
        other.player["finished"] = true

        manager.tick(bot)
        manager.tick(other)

        assertTrue(bot.noTask())
        assertEquals(BehaviourState.Wait(100, BehaviourState.Success), other.frame().state)
        assertNull(other.frame().completionCondition)
        assertSame(child, other.activity)
    }

    @Test
    fun `Ordinary nested setup finishes before its callers completion condition is evaluated`() {
        val manager = BotManager(world = FakeWorld())
        val bot = helper.testBot()
        manager.assign(bot, parent)
        manager.assign(bot, child, nested = true, completionCondition = goal)
        val setup = BehaviourFrame(Resolver("setup", 1, actions = listOf(BotWait(1))))
        bot.queue(setup)
        bot.player["finished"] = true

        manager.tick(bot)
        assertEquals(3, bot.frames.size)
        assertSame(setup, bot.frame())
        setup.state = BehaviourState.Success
        manager.tick(bot)
        assertEquals(2, bot.frames.size)
        manager.tick(bot)
        assertSame(parent, bot.frame().behaviour)
    }

    @Test
    fun `A resolver can inject a non skill goal on its own frame`() {
        val manager = BotManager(world = FakeWorld())
        val bot = helper.testBot()
        manager.assign(bot, parent)
        val resolver = Resolver("gather_items", 1, actions = child.actions)
        bot.queue(BehaviourFrame(resolver, completionCondition = goal))
        bot.player["finished"] = true

        manager.tick(bot)

        assertSame(parent, bot.frame().behaviour)
        assertSame(parent, bot.previous)
    }

    @Test
    fun `Completion stops the mode path suspended interaction and weak queues`() {
        val manager = BotManager(world = FakeWorld())
        val bot = helper.testBot()
        manager.assign(bot, child, completionCondition = goal)
        bot.frame().state = BehaviourState.Running
        bot.blocked.add(child.id)
        var modeStopped = false
        bot.player.mode = object : Mode {
            override fun tick() = Unit
            override fun stop(replacement: Mode) {
                modeStopped = true
            }
        }
        bot.player.steps.queueStep(Tile(1, 1))
        bot.player.weakQueue("skilling") {}
        bot.player.queue("unrelated") {}
        bot.player.suspension = mockk<Suspension.Delay>(relaxed = true)
        bot.player["finished"] = true

        manager.tick(bot)

        assertTrue(bot.noTask())
        assertFalse(bot.blocked.contains(child.id))
        assertTrue(modeStopped)
        assertEquals(EmptyMode, bot.player.mode)
        assertTrue(bot.player.steps.isEmpty())
        assertNull(bot.player.suspension)
        assertFalse(bot.player.queue.contains("skilling"))
        assertTrue(bot.player.queue.contains("unrelated"))
    }

    @Test
    fun `Nested activity uses its own reactive actions while its setup resolver is active`() {
        val reactive = BotRestart(emptyList(), BotVariable("never", true, false))
        val manager = BotManager(world = FakeWorld())
        val bot = helper.testBot()
        manager.assign(bot, parent.copy(actions = child.actions, reactive = listOf(reactive)))
        val parentFrame = bot.frame()
        parentFrame.index = 1
        manager.assign(bot, child.copy(reactive = listOf(reactive)), nested = true, completionCondition = goal)
        val childFrame = bot.frame()
        childFrame.index = 1
        val setup = BehaviourFrame(Resolver("setup", 1, actions = listOf(BotWait(1))))
        bot.queue(setup)

        manager.tick(bot)

        assertEquals(1, parentFrame.index, "Parent reactive ran while the training child was active")
        assertEquals(0, childFrame.index, "Child reactive was not run")
        assertSame(setup, bot.frame())
        assertNotNull(bot.combatContext)
    }

    @Test
    fun `Completion releases exactly one child slot and stopping the parent does not release it again`() {
        Areas.set(mapOf("field" to AreaDefinition("field", Rectangle(0, 0, 10, 10), params = mapOf(Params.BOT_CAPACITY to 2))))
        val activity = child.copy(setup = listOf(BotInArea("field")))
        val manager = BotManager(world = FakeWorld())
        val bot = helper.testBot()
        val survivor = helper.testBot()
        val replacement = helper.testBot()
        manager.assign(survivor, activity)
        manager.assign(bot, parent)
        manager.assign(bot, activity, nested = true, completionCondition = goal)
        assertFalse(manager.slots.hasFree(activity))
        bot.player["finished"] = true

        manager.tick(bot)
        assertTrue(manager.slots.hasFree(activity))
        manager.stop(bot)
        manager.assign(replacement, activity)
        assertFalse(manager.slots.hasFree(activity), "Survivor and replacement must still occupy both slots")
        manager.stop(survivor)
        manager.stop(replacement)
        assertTrue(manager.slots.hasFree(activity))
    }

    @Test
    fun `Satisfied completion does not hide a recorded failure or cancellation`() {
        for (reason in listOf(Reason.NoTarget, Reason.Timeout, Reason.Cancelled)) {
            val manager = BotManager(world = FakeWorld())
            val bot = helper.testBot()
            manager.assign(bot, parent)
            manager.assign(bot, child, nested = true, completionCondition = goal)
            bot.frame().fail(reason)
            bot.player["finished"] = true

            manager.tick(bot)

            assertTrue(bot.blocked.contains(child.id), "Failure $reason was replaced with success")
            if (reason == Reason.NoTarget) assertSame(parent, bot.frame().behaviour) else assertTrue(bot.noTask())
        }
    }
}
