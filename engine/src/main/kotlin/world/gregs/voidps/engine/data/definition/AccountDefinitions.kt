package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.previousName
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad

/**
 * Manages account definitions, display names, and clans.
 * Provides functionality to add, update, and retrieve account-related data.
 *
 * @property definitions A map storing account definitions keyed by display name.
 * @property displayNames A map storing display names keyed by account name.
 * @property clans A map storing clan data keyed by display name.
 */
class AccountDefinitions(
    private val definitions: MutableMap<String, AccountDefinition> = Object2ObjectOpenHashMap(),
    private val displayNames: MutableMap<String, String> = Object2ObjectOpenHashMap(),
    private val clans: MutableMap<String, Clan> = Object2ObjectOpenHashMap()
) {

    /**
     * Adds a player to the system by updating various collections including displayNames, definitions, and clans.
     *
     * @param player The player object containing account and clan information to be added.
     */
    fun add(player: Player) {
        displayNames[player.accountName.lowercase()] = player.name
        definitions[player.name.lowercase()] = AccountDefinition(player.accountName, player.name, player.previousName, player.passwordHash)
        clans[player.name.lowercase()] = Clan(
            owner = player.accountName,
            ownerDisplayName = player.name,
            name = player["clan_name", ""],
            friends = player.friends,
            ignores = player.ignores,
            joinRank = ClanRank.valueOf(player["clan_join_rank", "Anyone"]),
            talkRank = ClanRank.valueOf(player["clan_talk_rank", "Anyone"]),
            kickRank = ClanRank.valueOf(player["clan_kick_rank", "Corporeal"]),
            lootRank = ClanRank.valueOf(player["clan_loot_rank", "None"]),
            coinShare = player["coin_share_setting", false]
        )
    }

    /**
     * Updates the display name of an account and its associated metadata.
     *
     * @param accountName The name of the account whose display name is being updated.
     * @param newName The new display name to be associated with the account.
     * @param previousDisplayName The previous display name associated with the account.
     */
    fun update(accountName: String, newName: String, previousDisplayName: String) {
        val definition = definitions.remove(previousDisplayName.lowercase()) ?: return
        definitions[newName.lowercase()] = definition
        definition.displayName = newName
        definition.previousName = previousDisplayName
        displayNames[accountName.lowercase()] = newName
    }

    /**
     * Retrieves a clan by its display name.
     *
     * @param displayName The name of the clan to search for, case insensitive.
     * @return The clan object corresponding to the provided display name, or null if not found.
     */
    fun clan(displayName: String) = clans[displayName.lowercase()]

    /**
     * Retrieves an account definition by the given account name.
     *
     * @param account The name of the account to retrieve. This parameter is case-insensitive.
     * @return The account definition corresponding to the provided account name, or null if no match is found.
     */
    fun getByAccount(account: String): AccountDefinition? {
        return get(displayNames[account.lowercase()] ?: return null)
    }

    /**
     * Retrieves a value from the `definitions` map corresponding to the specified key.
     * The key is case-insensitive and will be converted to lowercase before lookup.
     *
     * @param key The key used to look up a value in the `definitions` map.
     * @return The value corresponding to the lowercase version of the key, or `null` if no matching entry is found.
     */
    fun get(key: String) = definitions[key.lowercase()]

    /**
     * Retrieves the value from the definitions map corresponding to the given key.
     * The key is converted to lowercase before lookup to ensure case-insensitivity.
     *
     * @param key The key used to retrieve the corresponding value from the definitions map.
     * @return The value associated with the lowercase version of the given key.
     * @throws NoSuchElementException if the key is not present in the definitions map.
     */
    fun getValue(key: String) = definitions.getValue(key.lowercase())

    /**
     * Loads account and clan definitions from the provided storage, processes them, and updates
     * internal structures with the appropriate mappings for further usage.
     *
     * @param storage The account storage containing definitions and clan data. Defaults to an implementation of `AccountStorage` retrieved from a dependency injection mechanism.
     * @return The updated account definitions after loading and processing.
     */
    fun load(storage: AccountStorage = get()): AccountDefinitions {
        timedLoad("account") {
            for ((name, definition) in storage.names()) {
                definitions[name.lowercase()] = definition
            }
            for (def in definitions.values) {
                displayNames[def.accountName.lowercase()] = def.displayName
            }
            for ((name, definition) in storage.clans()) {
                clans[name.lowercase()] = definition
            }
            definitions.size
        }
        return this
    }

}