package content.social.trade.exchange.limit

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import java.util.concurrent.TimeUnit

/**
 * Limit the number of specific items a player can buy every X hours
 * Items `limit` set in *.items.toml
 * Limit reset hours set by "grandExchange.buyLimit.hours" in game.properties
 */
class BuyLimits(private val itemDefinitions: ItemDefinitions) {

    val limits = Object2ObjectOpenHashMap<String, BuyLimit>()

    fun record(player: String, item: String, amount: Int) {
        limits.getOrPut("${player}_${item}") { BuyLimit() }.amount += amount
    }

    fun limit(player: String, item: String): Int {
        val limit = itemDefinitions.get(item).getOrNull<Int>("limit") ?: return -1
        return limit - (limits["${player}_${item}"]?.amount ?: 0)
    }

    fun tick() {
        val hours = Settings["grandExchange.buyLimit.hours", 4]
        if (hours <= 0) {
            return
        }
        val now = System.currentTimeMillis()
        for ((player, limit) in limits) {
            if (TimeUnit.MILLISECONDS.toHours(now - limit.timestamp) > hours) {
                limits.remove(player)
            }
        }
    }
}