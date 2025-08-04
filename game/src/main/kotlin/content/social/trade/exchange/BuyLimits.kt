package content.social.trade.exchange

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import java.util.concurrent.TimeUnit

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
        val now = System.currentTimeMillis()
        for ((player, limit) in limits) {
            if (TimeUnit.MILLISECONDS.toHours(now - limit.timestamp) > 4) {
                limits.remove(player)
            }
        }
    }
}