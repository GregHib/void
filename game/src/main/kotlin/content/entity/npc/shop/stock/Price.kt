package content.entity.npc.shop.stock

import org.koin.mp.KoinPlatformTools
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.math.max
import kotlin.math.min

object Price {
    private fun getRealItem(item: String): Int {
        val def = ItemDefinitions.get(item)
        if (def.noted) {
            return def.noteId
        }
        return def.id
    }

    fun getPrice(player: Player, item: String, index: Int, amount: Int): Int {
        val itemId = getRealItem(item)
        var price = EnumDefinitions.get("price_runes").int(itemId)
        if (player["shop_currency", "coins"] == "tokkul" && price != -1 && price > 0) {
            return price
        }
        price = EnumDefinitions.get("price_garden").int(itemId)
        if (price != -1 && price > 0) {
            return price
        }
        val def = ItemDefinitions.get(itemId)
        if (def.contains("skill_cape") || def.contains("skill_cape_t")) {
            return 99000
        }
        val count = player["amount_$index", -1]
        if (count == -1) {
            return -1
        }
        val max = 130
        val min = 100
        var actualAmount = when {
            count == 0 -> min
            amount == 0 -> max
            amount >= count -> min
            else -> (max - (max - min)) * (amount / count)
        }
        if (actualAmount < 0) {
            actualAmount = 0
        }
        actualAmount = max(min, min(max, actualAmount))
        price = def.cost * (actualAmount / 100)
        if (player["shop_currency", "coins"] == "tokkul") {
            price = 3 * price / 2
        }
        return max(price, 1)
    }

    fun of(item: String, currency: String = "coins"): Int {
        val itemId = getRealItem(item)
        val koin = KoinPlatformTools.defaultContext().getOrNull()
        if (koin != null) {
            var price = EnumDefinitions.get("price_runes").int(itemId)
            if (currency == "tokkul" && price != -1 && price > 0) {
                return price
            }
            price = EnumDefinitions.get("price_garden").int(itemId)
            if (price != -1 && price > 0) {
                return price
            }
        }
        val def = ItemDefinitions.get(itemId)
        if (def.contains("skill_cape") || def.contains("skill_cape_t")) {
            return 99000
        }
        val price = def.cost
        return max(price, 1)
    }
}
