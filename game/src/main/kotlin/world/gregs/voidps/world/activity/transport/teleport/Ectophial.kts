package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val areas: AreaDefinitions by inject()
val objects: GameObjects by inject()

on<InventoryOption>({ inventory == "inventory" && item.id == "ectophial" && option == "Empty" }, Priority.HIGH) { player: Player ->
    cancel()
    player.strongQueue("ectophial") {
        player.setAnimation("empty_ectophial")
        player.setGraphic("empty_ectophial")
        player.message("You empty the ectoplasm onto the ground around your feet...", ChatType.Filter)
        player.start("movement_delay", 4)
        pause(4)
        itemTeleport(player, inventory, slot, areas["ectophial_teleport"], "ectophial")
        pause(2)
        player.message("... and the world changes around you.", ChatType.Filter)
        pause(4)
        val ectofuntus = objects[Tile(3658, 3518), "ectofuntus"] ?: return@strongQueue
        player.mode = Interact(player, ectofuntus, ItemOnObject(player, ectofuntus, inventory, inventory, Item("ectophial_empty"), slot, inventory))
    }
}

on<ItemOnObject>({ operate && inventory == "inventory" && target.id == "ectofuntus" && item.id == "ectophial_empty" }) { player: Player ->
    arriveDelay()
    if (player.inventory.replace(itemSlot, item.id, "ectophial")) {
        player.setAnimation("take")
        player.message("You refill the ectophial from the Ectofuntus.")
    }
}