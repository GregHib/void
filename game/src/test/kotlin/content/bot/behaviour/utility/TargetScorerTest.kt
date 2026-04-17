package content.bot.behaviour.utility

import content.bot.behaviour.perception.BotCombatContext
import content.entity.combat.attacker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.type.Tile

class TargetScorerTest {

    private lateinit var scorer: Player

    @BeforeEach
    fun setup() {
        scorer = Player(tile = Tile(100, 100))
    }

    private fun newPlayer(tile: Tile, hp: Int = 99, maxHp: Int = 99): Player {
        val player = Player(tile = tile)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, maxHp))
        player.levels.set(Skill.Constitution, hp)
        return player
    }

    @Test
    fun `Picks lowest hp target with hp-weighted scorer`() {
        val full = newPlayer(Tile(100, 101), hp = 99)
        val low = newPlayer(Tile(100, 102), hp = 10)
        val context = BotCombatContext.EMPTY

        val ts = TargetScorer(
            listOf(
                TargetScorer.ScoreComponent(
                    TargetInput.TargetHpPercent,
                    UtilityCurve.Linear(min = 1.0, max = 0.0),
                    weight = 1.0,
                ),
            ),
        )

        assertEquals(low, ts.pick(scorer, listOf(full, low), context))
    }

    @Test
    fun `Picks closest target with distance-weighted scorer`() {
        val close = newPlayer(Tile(101, 100))
        val far = newPlayer(Tile(110, 100))
        val context = BotCombatContext.EMPTY

        val ts = TargetScorer(
            listOf(
                TargetScorer.ScoreComponent(
                    TargetInput.Distance,
                    UtilityCurve.Exponential(base = 0.9),
                    weight = 1.0,
                ),
            ),
        )

        assertEquals(close, ts.pick(scorer, listOf(far, close), context))
    }

    @Test
    fun `Empty candidate list returns null`() {
        val context = BotCombatContext.EMPTY
        val ts = TargetScorer(emptyList())
        assertNull(ts.pick(scorer, emptyList(), context))
    }

    @Test
    fun `AttackerOfAlly returns 1 when target attacks an ally`() {
        val ally = newPlayer(Tile(100, 100))
        val enemy = newPlayer(Tile(101, 100))
        val benign = newPlayer(Tile(102, 100))
        ally.attacker = enemy

        val context = BotCombatContext.EMPTY.copy(nearbyAllies = listOf(ally))
        val ts = TargetScorer(
            listOf(
                TargetScorer.ScoreComponent(TargetInput.AttackerOfAlly, UtilityCurve.Linear(), weight = 1.0),
            ),
        )

        assertEquals(enemy, ts.pick(scorer, listOf(benign, enemy), context))
    }
}
