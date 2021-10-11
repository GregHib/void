package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.player.equip.isSkillCape
import world.gregs.voidps.world.interact.entity.player.equip.isTrimmedSkillCape
import kotlin.math.max
import kotlin.math.min

object Price {
    private val itemDefs: ItemDefinitions by inject()
    private val enums: EnumDecoder by inject()

    fun datamap(id: Int, item: Int): Int {
        return enums.get(id).map?.get(item) as? Int ?: -1
    }

    fun getRealItem(item: Int): Int {
        val def = itemDefs.get(item)
        if (def.noted) {
            return def.noteId
        }
        return item
    }

    fun getPrice(player: Player, itemId: Int, index: Int, amount: Int): Int {
        val item = getRealItem(itemId)
        var price = datamap(731, item)
        if (player["shop_currency", "coins"] == "tokkul" && price != -1 && price > 0) {
            return price
        }
        price = datamap(733, item)
        if (price != -1 && price > 0) {
            return price
        }
        val def = itemDefs.get(item)
        if (def.isSkillCape() || def.isTrimmedSkillCape()) {
            return 99000
        }
        val count = player.getVar("amount_$index", -1)
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