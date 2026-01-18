package content.bot

import content.bot.interact.bank.closeBank
import content.bot.interact.bank.depositAll
import content.bot.interact.bank.openBank
import content.bot.interact.bank.withdrawCoins
import content.bot.interact.navigation.await
import content.bot.interact.shop.buy
import content.bot.interact.shop.closeShop
import content.bot.interact.shop.openNearestShop
import content.entity.player.bank.bank
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.network.client.instruction.InteractDialogue
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

object Bots : AutoCloseable {
    fun start(bot: Player) {
        start?.invoke(bot)
    }

    var start: ((Player) -> Unit)? = null

    override fun close() {
        start = null
    }
}

val Player.isBot: Boolean
    get() = contains("bot")

val Player.bot: Bot
    get() = get("bot")!!

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
        player.instructions.trySend(InteractInterface(interfaceId = 149, componentId = 0, itemId = def.id, itemSlot = index, option = 1))
    }
}

fun Bot.inventoryOption(item: String, option: String) {
    val index = player.inventory.indexOf(item)
    if (index != -1) {
        val def = get<ItemDefinitions>().getOrNull(item) ?: return
        player.instructions.trySend(InteractInterface(interfaceId = 149, componentId = 0, itemId = def.id, itemSlot = index, option = def.options.indexOf(option)))
    }
}

suspend fun Bot.npcOption(npc: NPC, option: String) {
    player.instructions.send(InteractNPC(npc.index, npc.def.options.indexOf(option) + 1))
}

suspend fun Bot.objectOption(obj: GameObject, option: String) {
    player.instructions.send(InteractObject(obj.def.id, obj.tile.x, obj.tile.y, obj.def.optionsIndex(option) + 1))
}

suspend fun Bot.dialogueOption(option: String) {
    val current = player.dialogue!!
    val definitions = get<InterfaceDefinitions>()
    val def = definitions.get(current)
    player.instructions.send(InteractDialogue(def.id, definitions.getComponentId(current, option)!!, -1))
    await("tick")
}

fun Bot.getObject(filter: (GameObject) -> Boolean): GameObject? {
    val objects = get<GameObjects>()
    for (zone in player.tile.zone.spiral(2)) {
        val obj = zone.toCuboid()
            .flatMap { tile -> objects.at(tile) }
            .firstOrNull(filter)
        if (obj != null) {
            return obj
        }
    }
    return null
}

fun Bot.getObjects(filter: (GameObject) -> Boolean): List<GameObject> {
    val objects = get<GameObjects>()
    val list = mutableListOf<GameObject>()
    for (zone in player.tile.zone.spiral(2)) {
        list.addAll(
            zone.toCuboid()
                .flatMap { tile -> objects.at(tile) }
                .filter(filter),
        )
    }
    return list
}
