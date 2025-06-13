package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan

/**
 * Saves and loads account data
 */
interface AccountStorage {

    /**
     * Migrate any old formats to new
     */
    fun migrate() {}

    /**
     * Loads all players account, display and previous names
     */
    fun names(): Map<String, AccountDefinition>

    /**
     * Loads all players clan chats
     */
    fun clans(): Map<String, Clan>

    /**
     * Batch saves accounts
     */
    fun save(accounts: List<PlayerSave>)

    /**
     * Checks if an account exists
     */
    fun exists(accountName: String): Boolean

    /**
     * Loads an account from the stored location
     */
    fun load(accountName: String): PlayerSave?
}
