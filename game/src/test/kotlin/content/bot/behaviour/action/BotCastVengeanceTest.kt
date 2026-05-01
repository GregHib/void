package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.timer.epochSeconds

class BotCastVengeanceTest {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.experience.set(Skill.Magic, Level.experience(Skill.Magic, 94))
        player.levels.set(Skill.Magic, 94)
    }

    @Test
    fun `Skips when magic below 94`() {
        player.levels.set(Skill.Magic, 93)
        var called = false
        val world = FakeWorld(execute = { _, _ ->
            called = true
            true
        })

        val state = BotCastVengeance.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(called)
    }

    @Test
    fun `Skips when vengeance already cast`() {
        player.set("vengeance", true)
        var called = false
        val world = FakeWorld(execute = { _, _ ->
            called = true
            true
        })

        val state = BotCastVengeance.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(called)
    }

    @Test
    fun `Skips while vengeance_delay cooldown active`() {
        player.start("vengeance_delay", 30, epochSeconds())
        var called = false
        val world = FakeWorld(execute = { _, _ ->
            called = true
            true
        })

        val state = BotCastVengeance.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(called)
    }
}
