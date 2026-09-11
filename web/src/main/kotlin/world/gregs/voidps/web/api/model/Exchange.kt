@file:UseSerializers(InstantSerializer::class)

package world.gregs.voidps.web.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

/**
 * The four tiles on the exchange home page. [marketIndex] is a fraction — `0.0134` is the site's
 * "+1.34%".
 */
@Serializable
data class MarketSummary(
    val trackedItems: Int,
    val worlds: Int,
    val valueTraded24h: Long,
    val tradesSettled24h: Long,
    val marketIndex: Double,
    val lastSampleAt: Instant,
    val nextSampleAt: Instant? = null,
)

@Serializable
data class ItemCategory(
    val id: String,
    val name: String,
    val code: String,
    val description: String? = null,
    val itemCount: Int,
)

/** A row in any of the exchange's lists. Prices and traded values are gp. */
@Serializable
data class ItemSummary(
    val id: String,
    val name: String,
    val category: String,
    val categoryName: String,
    val categoryCode: String,
    val examine: String? = null,
    val iconUrl: String? = null,
    val price: Long,
    val delta24h: Double,
    val volume24h: Long = 0,
    val valueTraded24h: Long = 0,
    val buyLimit: Int = 0,
    val members: Boolean = false,
)

/**
 * The item page. Repeats [ItemSummary]'s fields rather than nesting it — the spec composes the
 * two with `allOf`, so it is one flat object on the wire.
 */
@Serializable
data class ItemDetail(
    val id: String,
    val name: String,
    val category: String,
    val categoryName: String,
    val categoryCode: String,
    val examine: String? = null,
    val iconUrl: String? = null,
    val price: Long,
    val delta24h: Double,
    val volume24h: Long = 0,
    val valueTraded24h: Long = 0,
    val buyLimit: Int = 0,
    val members: Boolean = false,
    val description: String,
    val tradeable: Boolean = true,
    val buyPrice: Long,
    val sellPrice: Long,
    val margin: Long,
    val tax: Long,
    val highAlchemy: Long,
    val lowAlchemy: Long,
    val shopValue: Long,
    val buyLimitWindowHours: Int = 4,
    val updatedAt: Instant,
)

/** All four home-page panels in one response, since the page always shows them together. */
@Serializable
data class MarketHighlights(
    val topVolume: List<ItemSummary>,
    val risers: List<ItemSummary>,
    val fallers: List<ItemSummary>,
    val mostExpensive: List<ItemSummary>,
)

/** Search filters. A null [category] or [members] is no filter. */
data class ItemQuery(
    val name: String? = null,
    val category: String? = null,
    val members: Boolean? = null,
    val sort: ItemSort = ItemSort.Volume,
    val page: PageRequest = PageRequest(),
)

/** The five options on the search page's sort select. */
enum class ItemSort(val wire: String) {
    Volume("volume"),
    Price("price"),
    Gain("gain"),
    Loss("loss"),
    Name("name"),
    ;

    companion object {
        fun of(value: String?): ItemSort = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) } ?: Volume
    }
}

/** The price chart's range chips. */
@Serializable
enum class Timeframe(val wire: String) {
    @SerialName("24h")
    Day("24h"),

    @SerialName("7d")
    Week("7d"),

    @SerialName("30d")
    Month("30d"),

    @SerialName("1y")
    Year("1y"),

    @SerialName("all")
    All("all"),
    ;

    companion object {
        fun of(value: String?): Timeframe = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) } ?: Day
    }
}

/**
 * Points are oldest first and evenly spaced by [intervalSeconds], which is what lets the client
 * draw the path without carrying timestamps into its geometry. The last point is the live sample.
 */
@Serializable
data class PriceHistory(
    val itemId: String,
    val timeframe: Timeframe,
    val intervalSeconds: Int,
    val points: List<PricePoint>,
)

@Serializable
data class PricePoint(
    val at: Instant,
    val buy: Long,
    val sell: Long,
    val volume: Long,
)

/** The live ticker's poll response. */
@Serializable
data class PriceSnapshot(
    val sampledAt: Instant,
    val items: List<ItemPrice>,
)

@Serializable
data class ItemPrice(
    val id: String,
    val price: Long,
    val buy: Long,
    val sell: Long,
    val delta24h: Double,
)
