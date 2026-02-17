package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Condition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player

class BotRestartTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        player = Player()
        bot = Bot(player)
    }

    @Test
    fun `Success condition returns success`() {
        player.start("done", 10)

        val action = BotRestart(
            wait = emptyList(),
            success = Condition.Clock("done")
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Wait condition returning true keeps running`() {
        player.start("wait", 10)

        val action = BotRestart(
            wait = listOf(Condition.Clock("wait")),
            success = Condition.Clock("done")
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `Resets frame index when no wait matches`() {
        val frame = BehaviourFrame(FakeBehaviour())
        frame.index = 5

        val action = BotRestart(
            wait = emptyList(),
            success = Condition.Clock("done")
        )

        action.update(bot, FakeWorld(), frame)

        assertEquals(0, frame.index)
    }
}
