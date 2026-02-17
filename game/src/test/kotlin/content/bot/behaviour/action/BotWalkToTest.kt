package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class BotWalkToTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        player = Player(tile = Tile(3200, 3200))
        bot = Bot(player)
        GameLoop.tick = 0
        bot.mode = EmptyMode
        bot.steps.last = 0
        bot.steps.clear()
    }

    @Test
    fun `Start sends walk instruction and returns running`() {
        val action = BotWalkTo(3200, 3200)

        val state = action.start(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `Update returns success when within radius`() {
        val action = BotWalkTo(player.tile.x, player.tile.y)

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Returns running when not yet arrived`() {
        val action = BotWalkTo(player.tile.x + 10, player.tile.y + 10)

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
    }
}
