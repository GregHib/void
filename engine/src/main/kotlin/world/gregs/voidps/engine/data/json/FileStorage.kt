package world.gregs.voidps.engine.data.json

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.yaml.PlayerYamlReaderConfig
import world.gregs.voidps.engine.data.yaml.PlayerYamlWriterConfig
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration
import java.io.File

@Suppress("UNCHECKED_CAST")
class FileStorage(
    private val yaml: Yaml,
    private val directory: File,
    itemDefinitions: ItemDefinitions,
    experienceRate: Double
) : AccountStorage {
    private val config = object : YamlReaderConfiguration() {
        override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
            if (parentMap == "friends") {
                value as Map<String, Any>
                super.set(map, key, value.mapValues { ClanRank.valueOf(it.value as String) }, indent, parentMap)
            } else {
                super.set(map, key, value, indent, parentMap)
            }
        }
    }

    override fun names(): Map<String, AccountDefinition> {
        val files = directory.listFiles() ?: return emptyMap()
        val definitions: MutableMap<String, AccountDefinition> = Object2ObjectOpenHashMap()
        for (save in files) {
            val data = yaml.load<Map<String, Any>>(save.path, config)
            val variables = data["variables"] as MutableMap<String, Any>
            val accountName = data["accountName"] as String
            val displayName = variables.getOrDefault("display_name", accountName) as String
            definitions[displayName] = AccountDefinition(
                accountName = accountName,
                displayName = displayName,
                previousName = (variables.getOrDefault("name_history", emptyList<String>()) as List<String>).lastOrNull() ?: "",
                passwordHash = data["passwordHash"] as String
            )
        }
        return definitions
    }

    override fun clans(): Map<String, Clan> {
        val files = directory.listFiles() ?: return emptyMap()
        val clans: MutableMap<String, Clan> = Object2ObjectOpenHashMap()
        for (save in files) {
            val data = yaml.load<Map<String, Any>>(save.path, config)
            val variables = data["variables"] as MutableMap<String, Any>
            val accountName = data["accountName"] as String
            val displayName = variables.getOrDefault("display_name", accountName) as String
            clans[displayName] = Clan(
                owner = accountName,
                ownerDisplayName = displayName,
                name = variables.getOrDefault("clan_name", "") as String,
                friends = data["friends"] as MutableMap<String, ClanRank>,
                ignores = data["ignores"] as MutableList<String>,
                joinRank = ClanRank.valueOf(variables.getOrDefault("clan_join_rank", "Anyone") as String),
                talkRank = ClanRank.valueOf(variables.getOrDefault("clan_talk_rank", "Anyone") as String),
                kickRank = ClanRank.valueOf(variables.getOrDefault("clan_kick_rank", "Corporeal") as String),
                lootRank = ClanRank.valueOf(variables.getOrDefault("clan_loot_rank", "None") as String),
                coinShare = variables.getOrDefault("coin_share_setting", false) as Boolean
            )
        }
        return clans
    }

    override fun exists(accountName: String): Boolean {
        return directory.resolve(fileName(accountName)).exists()
    }

    private val writeConfig = PlayerYamlWriterConfig()
    private val readerConfig = PlayerYamlReaderConfig(itemDefinitions, experienceRate)

    override fun save(accounts: List<PlayerSave>) {
        for (account in accounts) {
            val file = directory.resolve(fileName(account.name))
            yaml.save(file, account, writeConfig)
        }
    }

    override fun load(accountName: String): PlayerSave? {
        val file = directory.resolve(fileName(accountName))
        if (!file.exists()) {
            return null
        }
        val map: Map<String, Any> = yaml.load(file.path, readerConfig)
        val experience = map["experience"] as Experience
        return PlayerSave(
            name = map["accountName"] as String,
            password = map["passwordHash"] as String,
            tile = map["tile"] as Tile,
            experience = experience.experience,
            blocked = experience.blocked.toList(),
            levels = (map["levels"] as Levels).levels,
            male = map["male"] as Boolean,
            looks = map["looks"] as IntArray,
            colours = map["colours"] as IntArray,
            variables = map["variables"] as MutableMap<String, Any>,
            inventories = (map["inventories"] as MutableMap<String, List<Item>>).mapValues { (_, value: List<Item>) ->
                value.toTypedArray()
            }.toMutableMap(),
            friends = map["friends"] as MutableMap<String, ClanRank>,
            ignores = map["ignores"] as MutableList<String>
        )
    }

    companion object {
        private fun fileName(name: String) = "${name.lowercase()}.json"
    }
}