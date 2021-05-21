import kotlinx.coroutines.isActive
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToNearest
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.task
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.network.instruct.CloseInterface
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.skill.woodcutting.Hatchet


on<ActionFinished>({ type == ActionType.Woodcutting }) { bot: Bot ->
    bot.resume("woodcutting")
}

on<InterfaceOpened>({ name == "bank" }) { bot: Bot ->
    bot.resume("bank")
}

task { bot ->
    while (isActive) {// While hasn't achieved goal
        setupInventory(bot)
        bot.goToNearest("trees")
        val map: MapArea = bot["area"]
        while (bot.player.inventory.isNotFull()) {
            val tree = bot.player.viewport.objects
                .filter { map.area.contains(it.tile) && it.def.options.contains("Chop down") }
                .minByOrNull { bot.tile.distanceTo(Distance.getNearest(it.tile, it.size, bot.tile)) }
            if (tree == null) {
                bot.await<Unit>("tick")
                continue
            }
            bot.player.instructions.emit(InteractObject(tree.id, tree.tile.x, tree.tile.y, 1))
            bot.await<Unit>("woodcutting")
        }
    }
}

suspend fun Bot.bank() {
    goToNearest("bank")
    val bank = player.viewport.objects.first { it.def.options[1] == "Use-quickly" }
    player.instructions.emit(InteractObject(objectId = bank.id, x = bank.tile.x, y = bank.tile.y, option = 2))
    await<Unit>("bank")
}

suspend fun setupInventory(bot: Bot) {
    bot.bank()
    bot.player.instructions.emit(InteractInterface(interfaceId = 762, componentId = 33, itemId = -1, itemSlot = -1, option = 0))// Deposit all
    bot.await<Unit>("tick")
    bot.await<Unit>("tick")
    if (!Hatchet.isHatchet(bot.player.equipped(EquipSlot.Weapon).name)) {
        val hatchet = bot.player.bank.getItems().withIndex().first { Hatchet.isHatchet(it.value.name) }
        bot.player.instructions.emit(InteractInterface(interfaceId = 762, componentId = 93, itemId = hatchet.value.id, itemSlot = hatchet.index, option = 0))
    }
    bot.await<Unit>("tick")
    bot.player.instructions.tryEmit(CloseInterface)
    bot.player.instructions.tryEmit(InteractInterface(interfaceId = 762, componentId = 43, itemId = -1, itemSlot = -1, option = 0))
}