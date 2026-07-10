package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.previousName
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import java.util.concurrent.ConcurrentHashMap

/**
 * Stores data about player accounts whether they're online or offline
 */
class AccountDefinitions(
    private val definitions: MutableMap<String, AccountDefinition> = ConcurrentHashMap(),
    val displayNames: MutableMap<String, String> = ConcurrentHashMap(),
    val clans: MutableMap<String, Clan> = ConcurrentHashMap(),
) {

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
            coinShare = player["coin_share_setting", false],
        )
    }

    fun update(accountName: String, newName: String, previousDisplayName: String) {
        val definition = definitions.remove(previousDisplayName.lowercase()) ?: return
        definitions[newName.lowercase()] = definition
        definition.displayName = newName
        definition.previousName = previousDisplayName
        displayNames[accountName.lowercase()] = newName
    }

    fun clan(displayName: String) = clans[displayName.lowercase()]

    fun getByAccount(accountName: String): AccountDefinition? {
        return get(displayNames[accountName.lowercase()] ?: return null)
    }

    fun get(displayName: String) = definitions[displayName.lowercase()]

    fun getValue(displayName: String) = definitions.getValue(displayName.lowercase())

    fun load(storage: Storage = get()): AccountDefinitions {
        timedLoad("account") {
            for ((_, definition) in storage.names()) {
                definitions[definition.displayName.lowercase()] = definition
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

    /**
     * Merges freshly loaded storage data into the in-memory cache so external
     * changes (website password resets, imported accounts) apply without a restart.
     * Entries whose account matches [skip] (online or mid-save) are left untouched
     * as their in-memory state may be newer than storage.
     * Add/update only; never removes entries. Must be called on the game thread.
     * @return number of definitions added or updated
     */
    fun merge(
        names: Map<String, AccountDefinition>,
        clanUpdates: Map<String, Clan>,
        skip: (accountName: String) -> Boolean,
    ): Int {
        var count = 0
        for ((_, definition) in names) {
            if (skip(definition.accountName)) {
                continue
            }
            val existing = definitions[definition.displayName.lowercase()]
            if (existing == null) {
                definitions[definition.displayName.lowercase()] = definition
            } else if (existing == definition) {
                continue
            } else {
                existing.displayName = definition.displayName
                existing.previousName = definition.previousName
                existing.passwordHash = definition.passwordHash
            }
            displayNames[definition.accountName.lowercase()] = definition.displayName
            count++
        }
        for ((name, clan) in clanUpdates) {
            if (skip(clan.owner)) {
                continue
            }
            val existing = clans[name.lowercase()]
            if (existing == null) {
                clans[name.lowercase()] = clan
            } else {
                existing.ownerDisplayName = clan.ownerDisplayName
                existing.name = clan.name
                existing.friends = clan.friends
                existing.ignores = clan.ignores
                existing.joinRank = clan.joinRank
                existing.talkRank = clan.talkRank
                existing.kickRank = clan.kickRank
                existing.lootRank = clan.lootRank
                existing.coinShare = clan.coinShare
            }
        }
        return count
    }
}
