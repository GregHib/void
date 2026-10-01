package world.gregs.voidps.web.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MarketSummary(
    val trackedItems: Int,
    val valueTraded24h: Long,
    val tradesSettled24h: Long,
    val marketIndex: Double,
    val lastSampleAt: String,
)

@Serializable
data class ItemCategory(
    val id: String,
    val name: String,
    val code: String,
    val itemCount: Int,
)

@Serializable
data class ItemSummary(
    val id: String,
    val name: String,
    val category: String,
    val categoryName: String,
    val categoryCode: String,
    val examine: String,
    val iconUrl: String?,
    val price: Int,
    val delta24h: Double,
    val volume24h: Long,
    val valueTraded24h: Long,
    val buyLimit: Int?,
    val members: Boolean,
)

@Serializable
data class ItemDetail(
    val id: String,
    val name: String,
    val category: String,
    val categoryName: String,
    val categoryCode: String,
    val examine: String,
    val iconUrl: String?,
    val price: Int,
    val delta24h: Double,
    val volume24h: Long,
    val valueTraded24h: Long,
    val buyLimit: Int?,
    val members: Boolean,
    val tradeable: Boolean,
    val buyPrice: Int,
    val sellPrice: Int,
    val margin: Int,
    val tax: Int,
    val highAlchemy: Int,
    val lowAlchemy: Int,
    val shopValue: Int,
    val buyLimitWindowHours: Int?,
    val updatedAt: String,
)

@Serializable
data class ItemPage(val pagination: Pagination, val items: List<ItemSummary>)

@Serializable
data class MarketHighlights(
    val topVolume: List<ItemSummary>,
    val risers: List<ItemSummary>,
    val fallers: List<ItemSummary>,
    val mostExpensive: List<ItemSummary>,
)

@Serializable
data class PricePoint(val at: String, val buy: Int, val sell: Int, val volume: Long)

@Serializable
data class PriceHistoryResponse(
    val itemId: String,
    val timeframe: String,
    val intervalSeconds: Int,
    val points: List<PricePoint>,
)

@Serializable
data class ItemPrice(val id: String, val price: Int, val buy: Int, val sell: Int, val delta24h: Double)

@Serializable
data class ItemPricesResponse(val sampledAt: String, val items: List<ItemPrice>)
