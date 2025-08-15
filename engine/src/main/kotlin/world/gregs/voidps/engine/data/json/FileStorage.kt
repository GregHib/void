package world.gregs.voidps.engine.data.json

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.*
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
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

class FileStorage(
    private val directory: File,
) : Storage {

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
                history = emptyList(),
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
        val buy = directory.resolve(Settings["storage.grand.exchange.offers.buy.path"])
        if (buy.exists()) {
            loadOffers(buy, offers, false)
        }
        val sell = directory.resolve(Settings["storage.grand.exchange.offers.sell.path"])
        if (sell.exists()) {
            loadOffers(sell, offers, true)
        }
        val file = directory.resolve(Settings["storage.grand.exchange.offers.path"])
        if (file.exists()) {
            Config.fileReader(file) {
                assert(key() == "counter")
                offers.counter = int()
            }
        }
        offers.removeInactive(days)
        return offers
    }

    override fun saveOffers(offers: Offers) {
        val buy = directory.resolve(Settings["storage.grand.exchange.offers.buy.path"])
        buy.mkdirs()
        saveOffers(buy, offers.buyByItem)
        val sell = directory.resolve(Settings["storage.grand.exchange.offers.sell.path"])
        sell.mkdirs()
        saveOffers(sell, offers.sellByItem)
        val file = directory.resolve(Settings["storage.grand.exchange.offers.path"])
        Config.fileWriter(file) {
            writePair("counter", offers.counter)
        }
    }

    private fun saveOffers(directory: File, byItem: Map<String, TreeMap<Int, MutableList<OpenOffer>>>) {
        for ((item, map) in byItem) {
            val file = directory.resolve("$item.toml")
            Config.fileWriter(file) {
                for ((price, list) in map) {
                    for (offer in list) {
                        writeSection(offer.id.toString())
                        writePair("price", price)
                        writePair("remaining", offer.remaining)
                        writePair("coins", offer.coins)
                        writePair("account", offer.account)
                        writePair("last_active", offer.lastActive)
                        write("\n")
                    }
                }
            }
        }
    }

    private fun loadOffers(directory: File, offers: Offers, sell: Boolean) {
        val files = directory.listFiles { _, name -> name.endsWith(".toml") } ?: return
        val map = if (sell) offers.sellByItem else offers.buyByItem
        for (file in files) {
            val tree = TreeMap<Int, MutableList<OpenOffer>>()
            val item = file.nameWithoutExtension
            Config.fileReader(file) {
                while (nextSection()) {
                    val id = section().toInt()
                    val (offer, price) = readOffer(id)
                    offers.add(id, item, price, sell)
                    tree.getOrPut(price) { mutableListOf() }.add(offer)
                }
            }
            map[item] = tree
        }
    }

    private fun ConfigReader.readOffer(id: Int): Pair<OpenOffer, Int> {
        var lastActive: Long = System.currentTimeMillis()
        var remaining = 0
        var coins = 0
        var price = 0
        var account = ""
        while (nextPair()) {
            when (val key = key()) {
                "last_active" -> lastActive = long()
                "remaining" -> remaining = int()
                "coins" -> coins = int()
                "account" -> account = string()
                "price" -> price = int()
                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
            }
        }
        return OpenOffer(
            id = id,
            remaining = remaining,
            coins = coins,
            lastActive = lastActive,
            account = account,
        ) to price
    }

    override fun claims(): Map<Int, Claim> {
        val file = directory.resolve(Settings["storage.grand.exchange.offers.claim.path"])
        if (!file.exists()) {
            return emptyMap()
        }
        val claims = mutableMapOf<Int, Claim>()
        Config.fileReader(file) {
            while (nextPair()) {
                val id = key().toInt()
                assert(nextElement())
                val amount = int()
                assert(nextElement())
                val coins = int()
                assert(!nextElement())
                claims[id] = Claim(amount = amount, price = coins)
            }
        }
        return claims
    }

    override fun saveClaims(claims: Map<Int, Claim>) {
        val file = directory.resolve(Settings["storage.grand.exchange.offers.claim.path"])
        file.parentFile.mkdirs()
        Config.fileWriter(file) {
            for ((id, claim) in claims) {
                writeKey(id.toString())
                list(2) { index ->
                    when (index) {
                        0 -> writeValue(claim.amount)
                        1 -> writeValue(claim.price)
                    }
                }
            }
        }
    }

    override fun priceHistory(): Map<String, PriceHistory> {
        val directory = directory.resolve(Settings["storage.grand.exchange.history.path"])
        val history = mutableMapOf<String, PriceHistory>()
        for (file in directory.listFiles() ?: return emptyMap()) {
            Config.fileReader(file) {
                val priceHistory = PriceHistory()
                while (nextSection()) {
                    val section = section()
                    val aggregates: MutableMap<Long, Aggregate> = when (section) {
                        "day" -> priceHistory.day
                        "week" -> priceHistory.week
                        "month" -> priceHistory.month
                        "year" -> priceHistory.year
                        else -> continue
                    }
                    while (nextPair()) {
                        val timestamp = key().toLong()
                        aggregates[timestamp] = readAggregate()
                    }
                }
                history[file.nameWithoutExtension] = priceHistory
            }
        }
        return history
    }

    private fun ConfigReader.readAggregate(): Aggregate {
        var open = 0
        var high = 0
        var low = Int.MAX_VALUE
        var close = 0
        var volume = 0L
        var count = 0
        var averageHigh = 0.0
        var averageLow = 0.0
        var volumeHigh = 0L
        var volumeLow = 0L
        var index = 0
        while (nextElement()) {
            when (index++) {
                0 -> open = int()
                1 -> high = int()
                2 -> low = int()
                3 -> close = int()
                4 -> volume = long()
                5 -> count = int()
                6 -> averageHigh = double()
                7 -> averageLow = double()
                8 -> volumeHigh = long()
                9 -> volumeLow = long()
            }
        }
        return Aggregate(
            open = open,
            high = high,
            low = low,
            close = close,
            volume = volume,
            count = count,
            averageHigh = averageHigh,
            averageLow = averageLow,
            volumeHigh = volumeHigh,
            volumeLow = volumeLow,
        )
    }

    override fun savePriceHistory(history: Map<String, PriceHistory>) {
        directory.resolve(Settings["storage.grand.exchange.history.path"]).mkdirs()
        for ((key, value) in history) {
            Config.fileWriter(directory.resolve("${Settings["storage.grand.exchange.history.path"]}/$key.toml")) {
                writeSection("day")
                write(value.day)
                writeSection("week")
                write(value.week)
                writeSection("month")
                write(value.month)
                writeSection("year")
                write(value.year)
            }
        }
    }

    private fun ConfigWriter.write(history: MutableMap<Long, Aggregate>) {
        for ((timestamp, aggregate) in history) {
            writeKey(timestamp.toString())
            list(10) { index ->
                when (index) {
                    0 -> writeValue(aggregate.open)
                    1 -> writeValue(aggregate.high)
                    2 -> writeValue(aggregate.low)
                    3 -> writeValue(aggregate.close)
                    4 -> writeValue(aggregate.volume)
                    5 -> writeValue(aggregate.count)
                    6 -> writeValue(aggregate.averageHigh)
                    7 -> writeValue(aggregate.averageLow)
                    8 -> writeValue(aggregate.volumeHigh)
                    9 -> writeValue(aggregate.volumeLow)
                }
            }
            write("\n")
        }
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
