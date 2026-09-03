package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.exchange.ExchangeOffer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import kotlin.test.assertEquals

internal class PlayerSaveTest {

    @Test
    fun `Snapshot doesn't share the player's friends`() {
        val player = Player(accountName = "name")
        player.friends["first"] = ClanRank.Friend

        val save = player.copy()
        player.friends["second"] = ClanRank.Friend

        assertEquals(mapOf("first" to ClanRank.Friend), save.friends, "Snapshot changed after the player edited their friends list")
    }

    @Test
    fun `Snapshot doesn't share the player's exchange offers`() {
        val player = Player(accountName = "name")
        player.offers[0] = ExchangeOffer(id = 1, item = "coins", amount = 10, price = 5)

        val save = player.copy()
        player.offers[0].completed = 7

        assertEquals(0, save.offers[0].completed, "Snapshot changed after the player's offer progressed")
    }
}
