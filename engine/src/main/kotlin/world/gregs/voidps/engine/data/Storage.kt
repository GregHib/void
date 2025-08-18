package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.exchange.Claim
import world.gregs.voidps.engine.data.exchange.OpenOffers
import world.gregs.voidps.engine.data.exchange.PriceHistory
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan

/**
 * Saves and loads account data
 */
interface Storage {

    /**
     * Loads all players account, display and previous names
     */
    fun names(): Map<String, AccountDefinition>

    /**
     * Loads all players clan chats
     */
    fun clans(): Map<String, Clan>

    /**
     * Loads all open grand exchange offers that haven't exceeded [days] since last updated
     */
    fun offers(days: Int): OpenOffers

    /**
     * Batch saves claims
     */
    fun saveOffers(offers: OpenOffers)

    /**
     * Loads all outstanding grand exchange claims
     */
    fun claims(): Map<Int, Claim>

    /**
     * Batch saves claims
     */
    fun saveClaims(claims: Map<Int, Claim>)

    /**
     * Loads exchange completion history
     */
    fun priceHistory(): Map<String, PriceHistory>

    /**
     * Batch saves item history
     */
    fun savePriceHistory(history: Map<String, PriceHistory>)

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
