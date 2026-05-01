package content.bot.behaviour.perception

import content.bot.Bot
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
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
    fun `No nearby players gives empty enemies-by-tile`() {
        val context = BotCombatContextBuilder.build(bot, radius = 5)

        assertTrue(context.enemiesByTile.isEmpty())
        assertNull(context.incomingAttackStyle)
    }
}
