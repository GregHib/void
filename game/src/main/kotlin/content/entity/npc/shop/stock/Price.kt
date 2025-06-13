package content.entity.npc.shop.stock

import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import kotlin.math.max
import kotlin.math.min

object Price {
    private fun getRealItem(item: String): Int {
        val def = get<ItemDefinitions>().get(item)
        if (def.noted) {
            return def.noteId
        }
        return def.id
    }

    fun getPrice(player: Player, item: String, index: Int, amount: Int): Int {
        val itemId = getRealItem(item)
        val enums = get<EnumDefinitions>()
        var price = enums.get("price_runes").getInt(itemId)
        if (player["shop_currency", "coins"] == "tokkul" && price != -1 && price > 0) {
            return price
        }
        price = enums.get("price_garden").getInt(itemId)
        if (price != -1 && price > 0) {
            return price
        }
        val def = get<ItemDefinitions>().get(itemId)
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
}
