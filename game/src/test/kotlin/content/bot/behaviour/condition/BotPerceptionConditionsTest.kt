package content.bot.behaviour.condition

import content.bot.Bot
import content.bot.behaviour.perception.BotCombatContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels

class BotPerceptionConditionsTest {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player["bot"] = bot
    }

    private fun setContext(incomingAttackStyle: String? = null) {
        bot.combatContext = BotCombatContext(
            incomingAttackStyle = incomingAttackStyle,
            enemiesByTile = emptyMap(),
        )
    }

    @Test
    fun `AttackerStyle matches incoming style`() {
        setContext(incomingAttackStyle = "melee")
        assertTrue(BotAttackerStyle(setOf("melee", "ranged")).check(player))
        assertFalse(BotAttackerStyle(setOf("magic")).check(player))
    }

    @Test
    fun `AttackerStyle returns false when no context`() {
        bot.combatContext = null
        assertFalse(BotAttackerStyle(setOf("melee")).check(player))
    }

    @Test
    fun `AttackerStyle returns false when no incoming attacker`() {
        setContext(incomingAttackStyle = null)
        assertFalse(BotAttackerStyle(setOf("melee")).check(player))
    }
}
