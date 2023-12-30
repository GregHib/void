package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interact.ItemOnItem
import world.gregs.voidps.engine.client.ui.interact.either
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val areas: AreaDefinitions by inject()

on<InventoryOption>({ item.id == "camulet" && option == "Rub" }) { player: Player ->
    if (jewelleryTeleport(player, inventory, slot, areas["camulet_teleport"])) {
        player.message("You rub the amulet...")
    } else {
        statement("Your Camulet has run out of teleport charges. You can renew them by applying camel dung.")
    }
}

on<InventoryOption>({ inventory == "inventory" && item.id == "camulet" && option == "Check-charge" }) { player: Player ->
    val charges = Degrade.charges(player, inventory, slot)
    player.message("Your Camulet has $charges ${"charge".plural(charges)} left.")
    if (charges == 0) {
        player.message("You can recharge it by applying camel dung.")
    }
}

on<ItemOnItem>({ fromInventory == "inventory" && toInventory == "inventory" && either { item, item2 -> item.id == "camulet" && item2.id == "ugthanki_dung" } }) { player: Player ->
    val slot = if (fromItem.id == "camulet") fromSlot else toSlot
    val charges = Degrade.charges(player, fromInventory, slot)
    if (charges == 4) {
        player.message("Your Camulet already has 4 charges.")
        return@on
    }
    if (player.inventory.replace("ugthanki_dung", "bucket")) {
        player.message("You recharge the Camulet using camel dung. Yuck!")
        player["camulet_charges"] = 4
    }
}