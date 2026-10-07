package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.AccountNames
import world.gregs.voidps.engine.data.definition.BotAccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.script.KoinMock
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class BotStorageTest : KoinMock() {
    @TempDir
    lateinit var directory: File

    private fun storage(): BotStorage {
        val players = directory.resolve("players").apply { mkdirs() }
        val bots = directory.resolve("bots").apply { mkdirs() }
        return BotStorage(FileStorage(players), FileStorage(bots))
    }

    @Test
    fun `Tagged bots save separately and only load from bot storage`() {
        val storage = storage()
        val human = Player(accountName = "human").copy()
        val bot = Player(accountName = "bot").apply { this["bot"] = true }.copy()
        storage.save(listOf(human, bot))

        assertTrue(directory.resolve("players/human.toml").exists())
        assertFalse(directory.resolve("players/bot.toml").exists())
        assertTrue(directory.resolve("bots/bot.toml").exists())
        assertTrue(assertNotNull(storage.loadBot("BOT")).bot)
        assertNull(storage.loadBot("human"))
        assertNull(storage.load("bot"))
        assertFalse(assertNotNull(storage.load("human")).bot)
        assertTrue(storage.accounts().all { !it.bot })

        // An untagged player file placed in the bot directory must not become a bot.
        human.save(directory.resolve("bots/human.toml"))
        assertNull(storage.loadBot("human"))
    }

    @Test
    fun `A bot save cannot overwrite a human save with the same name`() {
        val storage = storage()
        val human = Player(accountName = "shared").copy()
        val bot = Player(accountName = "shared").apply { this["bot"] = true }.copy()
        storage.save(listOf(human, bot))

        assertFalse(assertNotNull(storage.load("shared")).bot)
        assertTrue(assertNotNull(storage.loadBot("shared")).bot)
    }

    @Test
    fun `Both pools reserve account and display names with case and underscore normalization`() {
        val storage = storage()
        val human = Player(accountName = "human").copy().copy(variables = mapOf("display_name" to "Human Hero"))
        val bot = Player(accountName = "bot").apply { this["bot"] = true }.copy()
            .copy(variables = mapOf("display_name" to "Rune Mage"))
        storage.save(listOf(human, bot))

        val players = AccountDefinitions().load(storage)
        val bots = BotAccountDefinitions().load(storage)
        val names = AccountNames(players, bots)
        assertTrue(names.used("HUMAN"))
        assertTrue(names.used("human_hero"))
        assertTrue(names.used("BOT"))
        assertTrue(names.used("rune_mage"))
        assertTrue(names.isBot("Rune Mage"))
        assertFalse(players.used("bot"))
        assertFalse(names.used("Human Hero", players, "human"))
        assertTrue(names.used("Rune Mage", players, "human"))
        assertFalse(names.used("unused"))
    }
}
