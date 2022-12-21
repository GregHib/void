package world.gregs.voidps.bot

import world.gregs.voidps.bot.bank.closeBank
import world.gregs.voidps.bot.bank.depositAll
import world.gregs.voidps.bot.bank.openBank
import world.gregs.voidps.bot.bank.withdrawCoins
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.shop.buy
import world.gregs.voidps.bot.shop.closeShop
import world.gregs.voidps.bot.shop.openNearestShop
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentIntId
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.instruct.InteractDialogue
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.bank.bank

val Player.isBot: Boolean
    get() = contains("bot")

fun Bot.hasCoins(amount: Int, bank: Boolean = true): Boolean {
    if (player.inventory.contains("coins") && player.inventory.count("coins") >= amount) {
        return true
    }
    if (bank && player.bank.contains("coins") && player.bank.count("coins") >= amount) {
        return true
    }
    return false
}

suspend fun Bot.buyItem(item: String, amount: Int = 1): Boolean {
    if (player.inventory.isFull()) {
        openBank()
        depositAll()
        closeBank()
    }
    withdrawCoins()
    val success = try {
        openNearestShop(item)
    } catch (e: Exception) {
        false
    }
    if (success) {
        buy(item, amount)
        closeShop()
    }
    return success
}

fun Bot.equip(item: String) {
    val def = get<ItemDefinitions>().getOrNull(item) ?: return
    if (def.slot == EquipSlot.None) {
        return
    }
    val index = player.inventory.indexOf(item)
    if (index != -1) {
        player.instructions.tryEmit(InteractInterface(interfaceId = 149, componentId = 0, itemId = def.id, itemSlot = index, option = 1))
    }
}

fun Bot.inventoryOption(item: String, option: String) {
    val index = player.inventory.indexOf(item)
    if (index != -1) {
        val def = get<ItemDefinitions>().getOrNull(item) ?: return
        player.instructions.tryEmit(InteractInterface(interfaceId = 149, componentId = 0, itemId = def.id, itemSlot = index, option = def.options.indexOf(option)))
    }
}

suspend fun Bot.npcOption(npc: NPC, option: String) {
    player.instructions.emit(InteractNPC(npc.index, npc.def.options.indexOf(option) + 1))
}

suspend fun Bot.objectOption(obj: GameObject, option: String) {
    player.instructions.emit(InteractObject(obj.def.id, obj.tile.x, obj.tile.y, obj.def.optionsIndex(option) + 1))
}

suspend fun Bot.dialogueOption(option: String) {
    val current = player.dialogue!!
    val def = get<InterfaceDefinitions>().get(current)
    player.instructions.emit(InteractDialogue(def.actualId, def.getComponentIntId(option)!!, -1))
    await("tick")
}

fun Bot.getObject(filter: (GameObject) -> Boolean): GameObject? {
    val objects = get<Objects>()
    for (chunk in player.tile.chunk.spiral(2)) {
        val obj = objects[chunk, filter]
        if(obj != null) {
            return obj
        }
    }
    return null
}

fun Bot.getObjects(filter: (GameObject) -> Boolean): List<GameObject> {
    val objects = get<Objects>()
    val list = mutableListOf<GameObject>()
    for (chunk in player.tile.chunk.spiral(2)) {
        val obj = objects[chunk, filter]
        if(obj != null) {
            list.add(obj)
        }
    }
    return list
}
