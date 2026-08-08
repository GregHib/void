package world.gregs.voidps.engine.data

/**
 * A suspicious economy event flagged by a monitor for moderator review
 * @param type Flag identifier: "trade_value", "trade_pair", "price_anomaly" or "census_drift"
 * @param timestamp Epoch millisecond timestamp the flag was raised
 * @param tick Game tick the flag was raised
 * @param source Account name of the initiating player, if any
 * @param target Account name of the other player involved, if any
 * @param item Canonical item id involved, if any
 * @param value Primary coin or quantity value of the event
 * @param details Additional key=value context matching the audit log line
 */
data class EconomyFlag(
    val type: String,
    val timestamp: Long,
    val tick: Int,
    val source: String? = null,
    val target: String? = null,
    val item: String? = null,
    val value: Long = 0,
    val details: String? = null,
)
