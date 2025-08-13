package world.gregs.voidps.engine.data.json

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.exchange.*
import world.gregs.voidps.engine.data.yaml.PlayerYamlReaderConfig
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

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
                offers = Array(6) { ExchangeOffer() },
                history = emptyList()
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

    override fun offers(days: Int): Offers {
        val offers = Offers()
        val buy = directory.resolve("grand_exchange/buy_offers/")
        if (buy.exists()) {
            loadOffers(buy, offers, days, true)
        }
        val sell = directory.resolve("grand_exchange/sell_offers/")
        if (sell.exists()) {
            loadOffers(sell, offers, days, false)
        }
        return offers
    }

    private fun loadOffers(directory: File, offers: Offers, days: Int, buy: Boolean) {
        val now = System.currentTimeMillis()
        var max = 0
        val files = directory.listFiles { _, name -> name.endsWith(".toml") } ?: return
        val map = if (buy) offers.buyByItem else offers.sellByItem
        for (file in files) {
            val tree = TreeMap<Int, MutableList<OpenOffer>>()
            val item = file.nameWithoutExtension
            Config.fileReader(file) {
                while (nextSection()) {
                    val id = section().toInt()
                    val (offer, price) = readOffer(id)
                    if (id > max) {
                        max = id
                    }
                    offers.offers[id] = offer
                    // Only store active offers
                    if (days <= 0 || TimeUnit.MILLISECONDS.toDays(now - offer.lastActive) <= days) {
                        tree.getOrPut(price) { mutableListOf() }.add(offer)
                    }
                }
            }
            map[item] = tree
        }
        offers.counter = max(offers.counter, max)
    }

    private fun ConfigReader.readOffer(id: Int): Pair<OpenOffer, Int> {
        var amount = 0
        var lastActive: Long = System.currentTimeMillis()
        var completed = 0
        var coins = 0
        var price = 0
        var account = ""
        while (nextPair()) {
            when (val key = key()) {
                "amount" -> amount = int()
                "last_active" -> lastActive = long()
                "completed" -> completed = int()
                "coins" -> coins = int()
                "account" -> account = string()
                "price" -> price = int()
                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
            }
        }
        return OpenOffer(
            id = id,
            amount = amount,
            completed = completed,
            coins = coins,
            lastActive = lastActive,
            account = account
        ) to price
    }

    override fun claims(): Map<Int, Claim> {
        TODO("Not yet implemented")
    }

    override fun save(claims: Map<Int, Claim>) {
        TODO("Not yet implemented")
    }

    override fun itemHistory(): Map<String, ItemHistory> {
        TODO("Not yet implemented")
    }

    override fun save(history: Map<String, ItemHistory>) {
        TODO("Not yet implemented")
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
