package content.social.trade.exchange.limit

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.timer.epochMilliseconds
import java.util.concurrent.TimeUnit

/**
 * Limit the number of specific items a player can buy every X hours
 * Items `limit` set in *.items.toml
 * Limit reset hours set by "grandExchange.buyLimit.hours" in game.properties
 */
class BuyLimits {

    /**
     * [amount] of an item a player has bought, last updated [timestamp].
     */
    private data class BuyLimit(
        var amount: Int = 0,
        val timestamp: Long = epochMilliseconds(),
    )

    private val limits = Object2ObjectOpenHashMap<String, BuyLimit>()

    fun record(account: String, item: String, amount: Int, timestamp: Long = epochMilliseconds()) {
        limits.getOrPut("${account}_$item") { BuyLimit(timestamp = timestamp) }.amount += amount
    }

    fun limit(account: String, item: String): Int {
        val limit = ItemDefinitions.get(item).getOrNull<Int>("limit") ?: return -1
        return limit - (limits["${account}_$item"]?.amount ?: 0)
    }

    fun tick() {
        val hours = Settings["grandExchange.buyLimit.hours", 4]
        if (hours <= 0) {
            return
        }
        val now = epochMilliseconds()
        for ((player, limit) in limits) {
            if (TimeUnit.MILLISECONDS.toHours(now - limit.timestamp) > hours) {
                limits.remove(player)
            }
        }
    }

    fun clear() {
        limits.clear()
    }
}
