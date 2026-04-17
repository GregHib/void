package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.perception.BotCombatContext
import content.bot.behaviour.utility.TargetInput
import content.bot.behaviour.utility.TargetScorer
import content.bot.behaviour.utility.UtilityCurve
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.network.client.instruction.InteractPlayer

class BotFightPlayerScoringTest {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.levels.set(Skill.Constitution, 99)
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 99))
        player.options.set(1, "Attack")
    }

    private fun enemy(index: Int, hp: Int): Player {
        val p = Player(index = index)
        p.experience.player = p
        p.levels.link(p, PlayerLevels(p.experience))
        p.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 99))
        p.levels.set(Skill.Constitution, hp)
        return p
    }

    private fun lowHpScorer() = TargetScorer(
        listOf(
            TargetScorer.ScoreComponent(
                input = TargetInput.TargetHpPercent,
                curve = UtilityCurve.Linear(min = 1.0, max = 0.0),
                weight = 1.0,
            ),
        ),
    )

    @Test
    fun `Search picks lowest-hp enemy from context`() {
        val high = enemy(index = 1, hp = 90)
        val low = enemy(index = 2, hp = 20)
        val mid = enemy(index = 3, hp = 60)
        bot.combatContext = BotCombatContext.EMPTY.copy(
            ownHp = 99,
            ownMaxHp = 99,
            nearbyEnemies = listOf(high, low, mid),
        )
        bot.mode = EmptyMode

        var dispatched: InteractPlayer? = null
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is InteractPlayer) dispatched = instruction
                true
            },
        )

        val state = BotFightPlayer(targetScorer = lowHpScorer())
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
        assertNotNull(dispatched)
        assertEquals(2, dispatched!!.playerIndex)
    }

    @Test
    fun `Engaged-mode switches target when scorer prefers different enemy`() {
        val current = enemy(index = 1, hp = 95)
        val better = enemy(index = 2, hp = 10)
        bot.combatContext = BotCombatContext.EMPTY.copy(
            ownHp = 99,
            ownMaxHp = 99,
            nearbyEnemies = listOf(current, better),
        )
        val mode = mockk<PlayerOnPlayerInteract>(relaxed = true)
        every { mode.target } returns current
        bot.mode = mode

        var dispatched: InteractPlayer? = null
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is InteractPlayer) dispatched = instruction
                true
            },
        )

        val state = BotFightPlayer(targetScorer = lowHpScorer())
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
        assertNotNull(dispatched)
        assertEquals(2, dispatched!!.playerIndex)
    }

    @Test
    fun `Engaged-mode no switch when scorer agrees with current target`() {
        val current = enemy(index = 1, hp = 10)
        val other = enemy(index = 2, hp = 90)
        bot.combatContext = BotCombatContext.EMPTY.copy(
            ownHp = 99,
            ownMaxHp = 99,
            nearbyEnemies = listOf(current, other),
        )
        val mode = mockk<PlayerOnPlayerInteract>(relaxed = true)
        every { mode.target } returns current
        bot.mode = mode

        var dispatched: InteractPlayer? = null
        val world = FakeWorld(
            execute = { _, instruction ->
                if (instruction is InteractPlayer) dispatched = instruction
                true
            },
        )

        val state = BotFightPlayer(targetScorer = lowHpScorer())
            .update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertNull(dispatched)
    }

    @Test
    fun `Scorer pick returns lowest-hp candidate`() {
        val a = enemy(1, hp = 80)
        val b = enemy(2, hp = 25)
        val c = enemy(3, hp = 50)
        val context = BotCombatContext.EMPTY.copy(nearbyEnemies = listOf(a, b, c))

        val best = lowHpScorer().pick(player, context.nearbyEnemies, context)

        assertSame(b, best)
    }

    @Test
    fun `Scorer fallback to spiral search when no context`() {
        bot.combatContext = null
        bot.mode = EmptyMode

        val state = BotFightPlayer(targetScorer = lowHpScorer())
            .update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }
}
