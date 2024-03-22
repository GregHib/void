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
 * Stores data about player accounts whether they're online or offline
 */
class AccountDefinitions {

    private val definitions: MutableMap<String, AccountDefinition> = Object2ObjectOpenHashMap()
    private val displayNames: MutableMap<String, String> = Object2ObjectOpenHashMap()
    private val clans: MutableMap<String, Clan> = Object2ObjectOpenHashMap()

    fun add(player: Player) {
        displayNames[player.accountName] = player.name
        definitions[player.name] = AccountDefinition(player.accountName, player.name, player.previousName, player.passwordHash)
        clans[player.name] = Clan(
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

    fun update(accountName: String, newName: String, previousDisplayName: String) {
        val definition = definitions.remove(previousDisplayName) ?: return
        definitions[newName] = definition
        definition.displayName = newName
        definition.previousName = previousDisplayName
        displayNames[accountName] = newName
    }

    fun account(display: String) = getValue(display).accountName

    fun password(display: String) = getValue(display).passwordHash

    fun display(account: String) = displayNames[account]

    fun clan(displayName: String) = clans[displayName]

    fun getByAccount(account: String): AccountDefinition? {
        return get(displayNames[account] ?: return null)
    }

    fun get(key: String) = definitions[key]

    fun getValue(key: String) = definitions.getValue(key)

    fun load(storage: AccountStorage = get()): AccountDefinitions {
        timedLoad("account") {
            definitions.putAll(storage.names())
            for (def in definitions.values) {
                displayNames[def.accountName] = def.displayName
            }
            clans.putAll(storage.clans())
            definitions.size
        }
        return this
    }

}