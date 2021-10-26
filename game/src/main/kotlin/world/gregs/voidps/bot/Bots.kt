package world.gregs.voidps.bot

import world.gregs.voidps.bot.bank.withdrawCoins
import world.gregs.voidps.bot.shop.buy
import world.gregs.voidps.bot.shop.closeShop
import world.gregs.voidps.bot.shop.openNearestShop
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.world.activity.bank.bank

val Player.isBot: Boolean
    get() = contains("bot")

fun Bot.hasCoins(amount: Int, bank: Boolean = true): Boolean {
    if (player.inventory.contains("coins") && player.inventory.getCount("coins") >= amount) {
        return true
    }
    if (bank && player.bank.contains("coins") && player.bank.getCount("coins") >= amount) {
        return true
    }
    return false
}

suspend fun Bot.buyItem(item: String, amount: Int = 1) {
    withdrawCoins()
    openNearestShop(item)
    buy(item, amount)
    closeShop()
}

fun Bot.equip(item: String) {
    val id = get<ItemDefinitions>().getOrNull(item)?.id ?: return
    val index = player.inventory.indexOf(item)
    if (index != -1) {
        player.instructions.tryEmit(InteractInterface(interfaceId = 149, componentId = 0, itemId = id, itemSlot = index, option = 1))
    }
}