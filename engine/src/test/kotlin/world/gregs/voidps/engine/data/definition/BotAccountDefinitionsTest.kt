package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.script.KoinMock
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class BotAccountDefinitionsTest : KoinMock() {
    @Test
    fun `Human login with a different display name cannot be registered as a bot`() {
        val human = Player(accountName = "human_login").apply {
            this["display_name"] = "Human Hero"
            this["bot"] = true
        }
        val players = AccountDefinitions().apply { add(human) }
        val bots = BotAccountDefinitions()
        bots.add(human, players)
        assertNull(bots.getByAccount(human.accountName))
        assertFalse(AccountNames(players, bots).isBot(human.accountName))
    }

    @Test
    fun `Spawned bot still registers normally`() {
        val bots = BotAccountDefinitions()
        bots.add(Player(accountName = "spawned"), AccountDefinitions())
        assertNotNull(bots.getByAccount("spawned"))
    }
}
