package content.bot.owned

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * A companion's wages for its tasks. The owner pays coins into a slot's coffer up front (`::companion pay`), and
 * every item a task gathers or makes costs [percent] of its price, high alchemy or trade (`bots.owned.wage.*`).
 * Combat, following and world orders are free. The coffer is kept with the slot, through dismissal and death.
 */
object OwnedWages {
    /** Set on a bot while items are handed to it, so a gift is never charged as a gathered item. */
    const val RECEIVING = "owned_receiving"

    /** Set on a bot whose coffer ran dry mid-task; the next sync stops it. */
    const val UNPAID = "owned_unpaid"

    /** Set on a bot once it has warned that its wages are running low; cleared by a payment. */
    const val WARNED = "owned_wage_warned"

    /** Items' worth of wages a task must have before it starts, and the point the bot warns at. */
    const val START_ITEMS = 5
    const val WARN_ITEMS = 10

    /** [block], with whatever lands in [player]'s pack meanwhile handed over rather than gathered. */
    inline fun <T> receiving(player: Player, block: () -> T): T {
        player[RECEIVING] = true
        try {
            return block()
        } finally {
            player.clear(RECEIVING)
        }
    }

    fun percent(): Int = Settings["bots.owned.wage.percent", 20].coerceIn(0, 100)

    fun alch(): Boolean = Settings["bots.owned.wage.price", "trade"].equals("alch", ignoreCase = true)

    fun coffer(owner: Player, slot: Int): Int = owner["owned_bot_${slot}_coffer", 0]

    fun setCoffer(owner: Player, slot: Int, coins: Int) {
        owner["owned_bot_${slot}_coffer"] = coins.coerceAtLeast(0)
    }

    /** One [item]'s price as the wage reads it: its high alchemy value, or its trade price from [trade]. */
    fun price(item: String, trade: (String) -> Int): Int {
        val definition = ItemDefinitions.getOrNull(item) ?: return 0
        return if (alch()) (definition.cost * 0.6).toInt() else trade(item)
    }

    /** The wage for [amount] of [item], rounded up so a priced item never costs nothing; 0 for a worthless one. */
    fun wage(item: String, amount: Int, trade: (String) -> Int): Int {
        val percent = percent()
        val price = price(item, trade)
        if (percent == 0 || price <= 0 || amount <= 0) {
            return 0
        }
        val total = price.toLong() * amount * percent
        return ((total + 99) / 100).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    /** The wage for one of the dearest of [items]: what a task's tooltip and its start check go by. */
    fun perItem(items: List<String>, trade: (String) -> Int): Int = items.maxOfOrNull { wage(it, 1, trade) } ?: 0
}
