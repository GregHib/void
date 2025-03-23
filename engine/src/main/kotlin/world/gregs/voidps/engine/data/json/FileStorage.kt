package world.gregs.voidps.engine.data.json

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.*
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.yaml.PlayerYamlReaderConfig
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.skill.Skill
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
    private val directory: File
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
        for (save in files.filter { it.extension == "json" }) {
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
        val files = directory.listFiles()?.filter { it.extension == "json" } ?: return emptyMap()
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
        return directory.resolve("${accountName.lowercase()}.toml").exists()
    }

    private val readerConfig = PlayerYamlReaderConfig()

    override fun save(accounts: List<PlayerSave>) {
        for (account in accounts) {
            val file = directory.resolve("${account.name.lowercase()}.toml")
            account.save(file)
        }
    }

    override fun load(accountName: String): PlayerSave? {
        val file = directory.resolve("${accountName.lowercase()}.toml")
        if (!file.exists()) {
            val json = directory.resolve("${accountName.lowercase()}.json")
            if (!json.exists()) {
                return null
            }
            val map: Map<String, Any> = yaml.load(json.path, readerConfig)
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
        var name = ""
        var password = ""
        var tile = Tile.EMPTY
        val experience = DoubleArray(28)
        val blocked = ObjectArrayList<Skill>(0)
        val levels = IntArray(28)
        var male = true
        val looks = IntArray(3)
        val colours = IntArray(3)
        val variables = Object2ObjectOpenHashMap<String, Any>(50)
        val inventories = Object2ObjectOpenHashMap<String, Array<Item>>(4)
        val friends = Object2ObjectOpenHashMap<String, ClanRank>()
        val ignores = ObjectArrayList<String>()
        Config.fileReader(file) {
            while (nextPair()) {
                when (val key = key()) {
                    "accountName" -> name = string()
                    "passwordHash" -> password = string()
                    "experience" -> {
                        var index = 0
                        while (nextElement()) {
                            experience[index++] = double()
                        }
                    }
                    "blocked_skills" -> while (nextElement()) {
                        val skill = Skill.valueOf(string())
                        blocked.add(skill)
                    }
                    "levels" -> {
                        var index = 0
                        while (nextElement()) {
                            levels[index++] = int()
                        }
                    }
                    "male" -> male = boolean()
                    "looks" -> {
                        var index = 0
                        while (nextElement()) {
                            looks[index++] = int()
                        }
                    }
                    "colours" -> {
                        var index = 0
                        while (nextElement()) {
                            colours[index++] = int()
                        }
                    }
                    "tile" -> {
                        var x = 0
                        var y = 0
                        var level = 0
                        while (nextPair()) {
                            when (val k = key()) {
                                "x" -> x = int()
                                "y" -> y = int()
                                "level" -> level = int()
                                else -> throw IllegalArgumentException("Unexpected key: '$k' ${exception()}")
                            }
                        }
                        tile = Tile(x, y, level)
                    }
                    "variables" -> {
                        while (nextPair()) {
                            variables[key()] = value()
                        }
                    }
                    "inventories" -> {
                        while (nextPair()) {
                            val inv = key()
                            val items = ObjectArrayList<Item>()
                            while (nextElement()) {
                                var id = ""
                                var amount = 0
                                while (nextEntry()) {
                                    when (val itemKey = key()) {
                                        "id" -> id = string()
                                        "amount" -> amount = int()
                                        else -> throw IllegalArgumentException("Unexpected key: '$itemKey' ${exception()}")
                                    }
                                }
                                items.add(Item(id, amount))
                            }
                            inventories[inv] = items.toTypedArray()
                        }
                    }
                    "social" -> {
                        while (nextPair()) {
                            when (val socialKey = key()) {
                                "friends" -> while (nextEntry()) {
                                    val friend = key()
                                    val rank = string()
                                    friends[friend] = ClanRank.from(rank)
                                }
                                "ignores" -> while (nextElement()) {
                                    ignores.add(string())
                                }
                                else -> throw IllegalArgumentException("Unexpected key: '$socialKey' ${exception()}")
                            }
                        }

                    }
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                }
            }
        }
        return PlayerSave(name = name, password = password, tile = tile, experience = experience, blocked = blocked, levels = levels, male = male, looks = looks, colours = colours, variables = variables, inventories = inventories, friends = friends, ignores = ignores)
    }
}