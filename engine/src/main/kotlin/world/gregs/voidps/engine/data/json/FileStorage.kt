package world.gregs.voidps.engine.data.json

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.yaml.PlayerYamlReaderConfig
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml
import java.io.File

class FileStorage(
    private val directory: File,
) : AccountStorage {

    private val logger = InlineLogger()

    @Suppress("UNCHECKED_CAST")
    override fun migrate() {
        val files = directory.listFiles { _, b -> b.endsWith(".json") } ?: return
        val yaml = Yaml()
        val readerConfig = PlayerYamlReaderConfig()
        for (file in files) {
            val target = directory.resolve("${file.nameWithoutExtension}.toml")
            if (target.exists()) {
                logger.info { "Account file already exists for '${file.nameWithoutExtension}'." }
                continue
            }
            val map: Map<String, Any> = yaml.load(file.path, readerConfig)
            val experience = map["experience"] as Experience
            val save = PlayerSave(
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
                ignores = map["ignores"] as MutableList<String>,
            )
            save.save(target)
            file.delete()
            logger.info { "Migrated account '${file.nameWithoutExtension}'." }
        }
    }

    override fun names(): Map<String, AccountDefinition> {
        val files = directory.listFiles { _, b -> b.endsWith(".toml") } ?: return emptyMap()
        val definitions: MutableMap<String, AccountDefinition> = Object2ObjectOpenHashMap()
        for (save in files) {
            val data = PlayerSave.load(save)
            val variables = data.variables
            val accountName = data.name
            val displayName = variables.getOrDefault("display_name", accountName) as String
            definitions[accountName.lowercase()] = AccountDefinition(
                accountName = accountName,
                displayName = displayName,
                previousName = (variables.getOrDefault("name_history", emptyList<String>()) as List<String>).lastOrNull() ?: "",
                passwordHash = data.password,
            )
        }
        return definitions
    }

    override fun clans(): Map<String, Clan> {
        val files = directory.listFiles { _, b -> b.endsWith(".toml") } ?: return emptyMap()
        val clans: MutableMap<String, Clan> = Object2ObjectOpenHashMap()
        for (save in files) {
            val data = PlayerSave.load(save)
            val variables = data.variables
            val accountName = data.name
            val displayName = variables.getOrDefault("display_name", accountName) as String
            clans[accountName.lowercase()] = Clan(
                owner = accountName,
                ownerDisplayName = displayName,
                name = variables.getOrDefault("clan_name", "") as String,
                friends = data.friends,
                ignores = data.ignores,
                joinRank = ClanRank.valueOf(variables.getOrDefault("clan_join_rank", "Anyone") as String),
                talkRank = ClanRank.valueOf(variables.getOrDefault("clan_talk_rank", "Anyone") as String),
                kickRank = ClanRank.valueOf(variables.getOrDefault("clan_kick_rank", "Corporeal") as String),
                lootRank = ClanRank.valueOf(variables.getOrDefault("clan_loot_rank", "None") as String),
                coinShare = variables.getOrDefault("coin_share_setting", false) as Boolean,
            )
        }
        return clans
    }

    override fun exists(accountName: String): Boolean = directory.resolve("${accountName.lowercase()}.toml").exists()

    override fun save(accounts: List<PlayerSave>) {
        for (account in accounts) {
            val file = directory.resolve("${account.name.lowercase()}.toml")
            account.save(file)
        }
    }

    override fun load(accountName: String): PlayerSave? {
        val file = directory.resolve("${accountName.lowercase()}.toml")
        if (!file.exists()) {
            return null
        }
        return PlayerSave.load(file)
    }
}
