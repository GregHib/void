package world.gregs.voidps.web.exchange

import world.gregs.voidps.cache.definition.Category
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.exchange.Aggregate
import world.gregs.voidps.engine.data.exchange.PriceHistory
import world.gregs.voidps.web.PeriodicSnapshot
import world.gregs.voidps.web.api.model.*
import java.time.Instant
import kotlin.math.roundToInt

/**
 * Serves Grand Exchange listings, market highlights and price history from real item definitions
 * and [Storage.priceHistory]. Reading every item's history is too slow to do per request, and the
 * game only writes it to storage hourly anyway, so it's read into a [PeriodicSnapshot] rebuilt at
 * most once per `refreshMillis` along with every item's summary row pre-sorted each way the API
 * can sort them.
 *
 * There's no live "current price" tracked anywhere on disk - that only exists as in-memory state
 * inside a running game server's `ExchangeHistory.marketPrices` - so the guide price here is
 * derived from the most recent persisted [Aggregate] close, falling back to the item's
 * configured/shop price for anything that hasn't traded yet.
 */
class ExchangeService(
    private val storage: Storage,
    refreshMillis: Long = REFRESH_MS,
) {

    /** Every item the exchange can list: tradeable, named, and not a noted/lent duplicate of another entry. */
    private val pool: List<ItemDefinition> by lazy {
        ItemDefinitions.definitions
            .asSequence()
            .filter { it.exchangeable && it.stringId.isNotBlank() && it.name.isNotBlank() && it.name != "null" }
            .filter { !it.noted && !it.lent }
            .distinctBy { it.stringId }
            .sortedBy { it.name }
            .toList()
    }

    private val definitions: Map<String, ItemDefinition> by lazy { pool.associateBy { it.stringId } }

    private val snapshot = PeriodicSnapshot("exchange", refreshMillis) { market(storage.priceHistory()) }

    private class Market(
        val history: Map<String, PriceHistory>,
        /** Every [pool] item's row, keyed by id. */
        val rows: Map<String, ItemSummary>,
        /** Every row, ordered by each of the `/items` sort options. */
        val sorted: Map<String, List<ItemSummary>>,
        val summary: MarketSummary,
        val sampledAt: String,
    ) {
        val byVolume: List<ItemSummary> get() = sorted.getValue("volume")
    }

    private fun market(history: Map<String, PriceHistory>): Market {
        val sampledAt = Instant.now().toString()
        val rows = pool.map { row(it, history[it.stringId]) }
        val active = rows.filter { it.volume24h > 0 }
        val summary = MarketSummary(
            trackedItems = pool.size,
            valueTraded24h = rows.sumOf { it.valueTraded24h },
            tradesSettled24h = rows.sumOf { it.volume24h },
            marketIndex = if (active.isEmpty()) 0.0 else active.sumOf { it.delta24h } / active.size,
            lastSampleAt = sampledAt,
        )
        val sorted = mapOf(
            "volume" to rows.sortedByDescending { it.valueTraded24h },
            "price" to rows.sortedByDescending { it.price },
            "gain" to rows.sortedByDescending { it.delta24h },
            "loss" to rows.sortedBy { it.delta24h },
            "name" to rows.sortedBy { it.name.lowercase() },
        )
        return Market(history, rows.associateBy { it.id }, sorted, summary, sampledAt)
    }

    fun summary(): MarketSummary = snapshot.get().summary

    private val categories: List<ItemCategory> by lazy {
        val counts = pool.groupingBy { categoryOf(it) }.eachCount()
        ExchangeCategory.entries.map {
            ItemCategory(id = it.id, name = it.displayName, code = it.code, itemCount = counts[it.id] ?: 0)
        }
    }

    fun categories(): List<ItemCategory> = categories

    fun highlights(limit: Int): MarketHighlights {
        val sorted = snapshot.get().sorted
        return MarketHighlights(
            topVolume = sorted.getValue("volume").take(limit),
            risers = sorted.getValue("gain").take(limit),
            fallers = sorted.getValue("loss").take(limit),
            mostExpensive = sorted.getValue("price").take(limit),
        )
    }

    fun items(query: String?, category: String?, members: Boolean?, sort: String, page: Int, pageSize: Int): ItemPage {
        val market = snapshot.get()
        val q = query?.trim()?.lowercase()
        val filtered = (market.sorted[sort] ?: market.byVolume)
            .filter { q.isNullOrEmpty() || it.name.lowercase().contains(q) }
            .filter { category == null || category == "all" || it.category == category }
            .filter { members == null || it.members == members }
        val from = (page * pageSize).coerceIn(0, filtered.size)
        val to = (from + pageSize).coerceIn(from, filtered.size)
        return ItemPage(pagination = Pagination.of(page, pageSize, filtered.size), items = filtered.subList(from, to))
    }

    fun item(itemId: String): ItemDetail? {
        val definition = definitions[itemId] ?: return null
        val market = snapshot.get()
        val summary = market.rows[itemId] ?: return null
        val latest = latestAggregate(market.history[itemId])
        val price = summary.price
        val buyPrice = latest?.high ?: price
        val sellPrice = latest?.low?.takeIf { it != Int.MAX_VALUE } ?: price
        val tax = minOf(5_000_000, (price * 0.02).roundToInt())
        return ItemDetail(
            id = summary.id,
            name = summary.name,
            category = summary.category,
            categoryName = summary.categoryName,
            categoryCode = summary.categoryCode,
            examine = summary.examine,
            iconUrl = summary.iconUrl,
            price = price,
            delta24h = summary.delta24h,
            volume24h = summary.volume24h,
            valueTraded24h = summary.valueTraded24h,
            buyLimit = summary.buyLimit,
            members = summary.members,
            tradeable = definition.exchangeable,
            buyPrice = buyPrice,
            sellPrice = sellPrice,
            margin = buyPrice - sellPrice,
            tax = tax,
            highAlchemy = (definition.cost * 0.6).roundToInt(),
            lowAlchemy = (definition.cost * 0.4).roundToInt(),
            shopValue = definition.cost,
            buyLimitWindowHours = summary.buyLimit?.let { 4 },
            updatedAt = market.sampledAt,
        )
    }

    fun history(itemId: String, timeframe: String): PriceHistoryResponse? {
        val definition = definitions[itemId] ?: return null
        val history = snapshot.get().history[itemId]
        val (bucket, intervalSeconds) = when (timeframe) {
            "7d" -> (history?.week to 3_600)
            "30d" -> (history?.month to 21_600)
            "1y", "all" -> (history?.year to 86_400)
            else -> (history?.day to 300)
        }
        val points = (bucket ?: emptyMap()).entries
            .sortedBy { it.key }
            .map { (timestamp, aggregate) ->
                PricePoint(
                    at = Instant.ofEpochMilli(timestamp).toString(),
                    buy = aggregate.high,
                    sell = aggregate.low.takeIf { it != Int.MAX_VALUE } ?: aggregate.high,
                    volume = aggregate.volume,
                )
            }
        return PriceHistoryResponse(itemId = definition.stringId, timeframe = timeframe, intervalSeconds = intervalSeconds, points = points)
    }

    fun related(itemId: String, limit: Int): List<ItemSummary>? {
        val definition = definitions[itemId] ?: return null
        val category = categoryOf(definition)
        return snapshot.get().byVolume
            .asSequence()
            .filter { it.id != definition.stringId && it.category == category }
            .take(limit)
            .toList()
    }

    fun prices(ids: List<String>): ItemPricesResponse {
        val market = snapshot.get()
        val items = ids.mapNotNull { id ->
            val row = market.rows[id] ?: return@mapNotNull null
            val latest = latestAggregate(market.history[id])
            ItemPrice(
                id = row.id,
                price = row.price,
                buy = latest?.high ?: row.price,
                sell = latest?.low?.takeIf { it != Int.MAX_VALUE } ?: row.price,
                delta24h = row.delta24h,
            )
        }
        return ItemPricesResponse(sampledAt = market.sampledAt, items = items)
    }

    private fun row(definition: ItemDefinition, history: PriceHistory?): ItemSummary {
        val category = ExchangeCategory.of(categoryOf(definition))
        val day = history?.day ?: emptyMap()
        val volume24h = day.values.sumOf { it.volume }
        val price = guidePrice(definition, history)
        return ItemSummary(
            id = definition.stringId,
            name = definition.name,
            category = category.id,
            categoryName = category.displayName,
            categoryCode = category.code,
            examine = definition.getOrNull(Params.EXAMINE) ?: "",
            iconUrl = null,
            price = price,
            delta24h = delta24h(history),
            volume24h = volume24h,
            valueTraded24h = volume24h * price,
            buyLimit = definition.getOrNull(Params.LIMIT),
            members = definition.members,
        )
    }

    /** The most recent trade price, or the item's configured/shop price if it's never traded. */
    private fun guidePrice(definition: ItemDefinition, history: PriceHistory?): Int {
        val aggregate = latestAggregate(history) ?: return definition[Params.PRICE, definition.cost].coerceAtLeast(1)
        return aggregate.close.coerceAtLeast(1)
    }

    /** The single most recently updated bucket across every timeframe still retained for the item. */
    private fun latestAggregate(history: PriceHistory?): Aggregate? {
        if (history == null) return null
        return sequenceOf(history.day, history.week, history.month, history.year)
            .flatMap { it.entries.asSequence() }
            .maxByOrNull { it.key }
            ?.value
    }

    /** Change over the oldest-to-newest span still kept in the 5-minute `day` bucket (up to 24h, see [PriceHistory]). */
    private fun delta24h(history: PriceHistory?): Double {
        val day = history?.day ?: return 0.0
        if (day.size < 2) return 0.0
        val oldest = day.entries.minByOrNull { it.key }?.value ?: return 0.0
        val newest = day.entries.maxByOrNull { it.key }?.value ?: return 0.0
        if (oldest.close <= 0) return 0.0
        return (newest.close - oldest.close).toDouble() / oldest.close
    }

    private fun categoryOf(definition: ItemDefinition): String {
        val categories = definition.getOrNull<Set<String>>(Params.CATEGORIES) ?: emptySet()
        return when {
            definition.stringId.endsWith("_rune") || categories.contains(RUNECRAFTING) -> "runes"
            categories.any { it in WEAPON_CATEGORIES } -> "weapons"
            categories.any { it in ARMOUR_CATEGORIES } -> "armour"
            categories.any { it in CONSUMABLE_CATEGORIES } -> "consumables"
            categories.any { it in RESOURCE_CATEGORIES } -> "resources"
            else -> "misc"
        }
    }

    companion object {
        const val REFRESH_MS = 300_000L
        private val RUNECRAFTING = Category.name(Category.RUNECRAFTING)
        private val WEAPON_CATEGORIES = setOf(
            Category.MAGIC_WEAPON, Category.RANGE_WEAPON, Category.THROWABLE, Category.ARROW, Category.BOLT,
            Category.MELEE_WEAPON_LOW, Category.MELEE_WEAPON_MID, Category.MELEE_WEAPON_HIGH,
        ).map { Category.name(it) }.toSet()
        private val ARMOUR_CATEGORIES = setOf(
            Category.MAGIC_ARMOUR, Category.RANGE_ARMOUR, Category.PRAYER_ARMOUR, Category.JEWELLERY,
            Category.MELEE_ARMOUR_LOW, Category.MELEE_ARMOUR_MID, Category.MELEE_ARMOUR_HIGH,
        ).map { Category.name(it) }.toSet()
        private val CONSUMABLE_CATEGORIES = setOf(
            Category.POTION, Category.EDIBLE, Category.UNCOOKED_FOOD, Category.PRAYER_CONSUMABLE, Category.HERBLORE,
        ).map { Category.name(it) }.toSet()
        private val RESOURCE_CATEGORIES = setOf(
            Category.MINING_SMELTING, Category.SEED, Category.LOG, Category.FLETCHING, Category.CRAFTING,
            Category.HUNTER_REQUIRED_ITEM, Category.HUNTER_REWARD, Category.CONSTRUCTION, Category.CONSTRUCTION_PLANT,
            Category.CONSTRUCTION_STORABLE_CLOTHES, Category.SUMMONING_POUCHES, Category.SUMMONING_SCROLL, Category.FURNITURE,
        ).map { Category.name(it) }.toSet()
    }
}

enum class ExchangeCategory(val id: String, val displayName: String, val code: String) {
    Weapons("weapons", "Weapons", "WPN"),
    Armour("armour", "Armour", "ARM"),
    Runes("runes", "Runes", "RUN"),
    Consumables("consumables", "Consumables", "PTN"),
    Resources("resources", "Resources", "RES"),
    Misc("misc", "Misc", "MSC");

    companion object {
        fun of(id: String): ExchangeCategory = entries.firstOrNull { it.id == id } ?: Misc
    }
}
