package world.gregs.voidps.bot.bank

import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.cancel
import world.gregs.voidps.bot.navigation.goToNearest
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.instruct.EnterInt
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.world.activity.bank.bank

private fun getItemId(id: String): Int? = get<ItemDefinitions>().getOrNull(id)?.id

suspend fun Bot.openBank() {
    if (player.menu == "bank") {
        return
    }
    goToNearest("bank")
    val bank = getObject { it.def.contains(1, "Use-quickly") } ?: return cancel()
    objectOption(bank, "Use-quickly")
    await("bank")
}

suspend fun Bot.depositAll() {
    if (player.inventory.isEmpty()) {
        return
    }
    clickInterface(762, 33, 0)
    await("tick")
    await("tick")
}

suspend fun Bot.depositWornItems() {
    if (player.equipment.isEmpty()) {
        return
    }
    clickInterface(762, 35, 0)
    await("tick")
    await("tick")
}

suspend fun Bot.depositAll(item: String, slot: Int = player.inventory.indexOf(item)) {
    if (slot == -1) {
        return
    }
    player.instructions.emit(InteractInterface(interfaceId = 763, componentId = 0, itemId = getItemId(item) ?: return, itemSlot = slot, option = 5))
    await("tick")
}

suspend fun Bot.deposit(item: String, slot: Int = player.inventory.indexOf(item), amount: Int = 1) {
    if (slot == -1) {
        return
    }
    val option = when (amount) {
        1 -> 0
        5 -> 1
        10 -> 2
        else -> 4
    }
    player.instructions.emit(InteractInterface(interfaceId = 763, componentId = 0, itemId = getItemId(item) ?: return, itemSlot = slot, option = option))
    if (option == 4) {
        await("tick")
        player.instructions.emit(EnterInt(value = amount))
    }
    await("tick")
}

suspend fun Bot.withdraw(item: String, slot: Int = player.bank.indexOf(item), amount: Int = 1) {
    if (slot == -1) {
        return
    }
    val option = when (amount) {
        1 -> 0
        5 -> 1
        10 -> 2
        else -> 4
    }
    player.instructions.emit(InteractInterface(interfaceId = 762, componentId = 93, itemId = getItemId(item) ?: return, itemSlot = slot, option = option))
    if (option == 4) {
        await("tick")
        player.instructions.emit(EnterInt(value = amount))
    }
    await("tick")
}

suspend fun Bot.withdrawAll(vararg items: String) {
    var open = false
    for (item in items) {
        if (player.bank.contains(item) && !open) {
            openBank()
            open = true
        }
        withdrawAll(item)
    }
    if (open) {
        closeBank()
    }
}

suspend fun Bot.withdrawAll(item: String, slot: Int = player.bank.indexOf(item)) {
    if (slot == -1) {
        return
    }
    player.instructions.emit(InteractInterface(interfaceId = 762, componentId = 93, itemId = getItemId(item) ?: return, itemSlot = slot, option = 5))
    await("tick")
}

suspend fun Bot.withdrawAllButOne(item: String, slot: Int = player.bank.indexOf(item)) {
    if (slot == -1) {
        return
    }
    player.instructions.emit(InteractInterface(interfaceId = 762, componentId = 93, itemId = getItemId(item) ?: return, itemSlot = slot, option = 6))
    await("tick")
}

suspend fun Bot.closeBank() = closeInterface(762, 43)

suspend fun Bot.withdrawCoins() {
    if (!player.inventory.contains("coins")) {
        openBank()
        withdrawAllButOne("coins")
        closeBank()
    }
}