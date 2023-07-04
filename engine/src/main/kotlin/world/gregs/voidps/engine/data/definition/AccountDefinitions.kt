package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.previousName
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration
import java.io.File

/**
 * Stores data about player accounts whether they're online or offline
 */
class AccountDefinitions {

    private val definitions: MutableMap<String, AccountDefinition> = Object2ObjectOpenHashMap()
    private val displayNames: MutableMap<String, String> = Object2ObjectOpenHashMap()
    private val clans: MutableMap<String, Clan> = Object2ObjectOpenHashMap()

    fun add(player: Player) {
        displayNames[player.accountName] = player.name
        definitions[player.name] = AccountDefinition(player.accountName, player.name, player.previousName)
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

    fun add(
        accountName: String,
        displayName: String,
        previousName: String,
        friends: Map<String, ClanRank>,
        ignores: List<String>,
        clanName: String,
        joinRank: ClanRank,
        talkRank: ClanRank,
        kickRank: ClanRank,
        lootRank: ClanRank,
        coinShare: Boolean
    ) {
        displayNames[accountName] = displayName
        definitions[displayName] = AccountDefinition(accountName, displayName, previousName)
        clans[displayName] = Clan(
            owner = accountName,
            ownerDisplayName = displayName,
            name = clanName,
            friends = friends,
            ignores = ignores,
            joinRank = joinRank,
            talkRank = talkRank,
            kickRank = kickRank,
            lootRank = lootRank,
            coinShare = coinShare,
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

    fun display(account: String) = displayNames[account]

    fun clan(displayName: String) = clans[displayName]

    fun getByAccount(account: String): AccountDefinition? {
        return get(displayNames[account] ?: return null)
    }

    fun get(key: String) = definitions[key]

    fun getValue(key: String) = definitions.getValue(key)

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = getProperty("savePath")): AccountDefinitions {
        timedLoad("account") {
            val config = object : YamlReaderConfiguration() {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (parentMap == "friends") {
                        value as Map<String, Any>
                        super.set(map, key, value.mapValues { ClanRank.of(it.value as String) }, indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            for (save in File(path).listFiles() ?: return@timedLoad 0) {
                val data = yaml.load<Map<String, Any>>(save.path, config)
                val variables = data["variables"] as MutableMap<String, Any>
                val accountName = data["accountName"] as String
                add(
                    accountName = accountName,
                    displayName = variables.getOrDefault("display_name", accountName) as String,
                    previousName = (variables.getOrDefault("name_history", emptyList<String>()) as List<String>).firstOrNull() ?: "",
                    friends = data["friends"] as MutableMap<String, ClanRank>,
                    ignores = data["ignores"] as MutableList<String>,
                    clanName = variables.getOrDefault("clan_name", "") as String,
                    joinRank = ClanRank.valueOf(variables.getOrDefault("clan_join_rank", "Anyone") as String),
                    talkRank = ClanRank.valueOf(variables.getOrDefault("clan_talk_rank", "Anyone") as String),
                    kickRank = ClanRank.valueOf(variables.getOrDefault("clan_kick_rank", "Corporeal") as String),
                    lootRank = ClanRank.valueOf(variables.getOrDefault("clan_loot_rank", "None") as String),
                    coinShare = variables.getOrDefault("coin_share_setting", false) as Boolean
                )
            }
            definitions.size
        }
        return this
    }

}