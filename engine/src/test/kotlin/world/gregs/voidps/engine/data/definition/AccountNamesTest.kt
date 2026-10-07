package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.script.KoinMock
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AccountNamesTest : KoinMock() {
    @Test
    fun `New bots reserve names immediately and can resume after release`() {
        val players = AccountDefinitions()
        val bots = BotAccountDefinitions()
        val names = AccountNames(players, bots)
        assertTrue(names.reserveBot("Rune Mage"))
        assertTrue(names.used("rune_mage"))
        assertFalse(names.reserveBot("RUNE MAGE"))
        names.releaseBot("Rune Mage")
        assertTrue(names.reserveBot("Rune Mage"))
    }

    @Test
    fun `Failed first spawn releases its identity but failed saved bot load preserves ownership`() {
        val bots = BotAccountDefinitions().apply { reserve("saved") }
        val names = AccountNames(AccountDefinitions(), bots)
        assertTrue(names.reserveBot("fresh"))
        names.releaseBot("fresh", discardNew = true)
        assertFalse(names.used("fresh"))
        assertTrue(names.reserveBot("saved"))
        names.releaseBot("saved", discardNew = true)
        assertTrue(names.used("saved"))
        assertTrue(names.reserveBot("saved"))
    }

    @Test
    fun `Bots cannot claim human login display or previous names`() {
        val players = AccountDefinitions(mutableMapOf("hero" to AccountDefinition("login", "Hero", "Old Hero", "hash")))
        val names = AccountNames(players, BotAccountDefinitions())
        assertFalse(names.reserveBot("LOGIN"))
        assertFalse(names.reserveBot("hero"))
        assertFalse(names.reserveBot("old_hero"))
    }

    @Test
    fun `Renaming updates shared name reservations and excludes only the correct owner`() {
        val players = AccountDefinitions(mutableMapOf("hero" to AccountDefinition("login", "Hero", "Old Hero", "hash")))
        val bots = BotAccountDefinitions().apply { reserve("Bot") }
        val names = AccountNames(players, bots)
        players.update("login", "New Hero", "Hero")
        assertTrue(names.used("new_hero"))
        assertTrue(names.used("Hero"))
        assertFalse(names.used("Old Hero"))
        assertFalse(names.used("New Hero", players, "login"))
        assertTrue(names.used("Bot", players, "Bot"))
        bots.update("Bot", "Rune Bot", "Bot")
        assertTrue(names.used("Rune Bot"))
        assertTrue(names.used("Bot"))
    }

    @Test
    fun `New human accounts reserve their names in memory`() {
        val players = AccountDefinitions()
        val names = AccountNames(players, BotAccountDefinitions())
        players.add(Player(accountName = "human"))
        assertTrue(names.used("HUMAN"))
        assertFalse(names.reserveBot("human"))
    }
}
