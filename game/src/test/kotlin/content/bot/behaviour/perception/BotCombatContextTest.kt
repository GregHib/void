package content.bot.behaviour.perception

import content.bot.Bot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels

class BotCombatContextTest {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        Players.clear()
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
    }

    @Test
    fun `Builds context with own hp and prayer levels`() {
        player.levels.set(Skill.Constitution, 70)
        player.levels.set(Skill.Prayer, 43)

        val context = BotCombatContextBuilder.build(bot, radius = 1)

        assertEquals(70, context.ownHp)
        assertEquals(100, context.ownMaxHp)
        assertEquals(43, context.ownPrayerPoints)
        assertEquals(0.7, context.ownHpPercent, 0.001)
    }

    @Test
    fun `No nearby players gives empty enemies and allies`() {
        val context = BotCombatContextBuilder.build(bot, radius = 5)

        assertTrue(context.nearbyEnemies.isEmpty())
        assertTrue(context.nearbyAllies.isEmpty())
        assertTrue(context.enemiesByTile.isEmpty())
        assertNull(context.incomingAttacker)
        assertNull(context.incomingAttackStyle)
    }

    @Test
    fun `Empty context has zero hp and percent`() {
        val context = BotCombatContext.EMPTY

        assertEquals(0, context.ownHp)
        assertEquals(0, context.ownMaxHp)
        assertEquals(0.0, context.ownHpPercent, 0.001)
        assertEquals(-1, context.lastHitReceivedTick)
    }
}
