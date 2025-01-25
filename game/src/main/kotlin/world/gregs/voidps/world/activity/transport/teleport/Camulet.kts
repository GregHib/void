package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem

val areas: AreaDefinitions by inject()

inventoryItem("Rub", "camulet") {
    if (jewelleryTeleport(player, inventory, slot, areas["camulet_teleport"])) {
        player.message("You rub the amulet...")
    } else {
        statement("Your Camulet has run out of teleport charges. You can renew them by applying camel dung.")
    }
}

inventoryItem("Check-charge", "camulet", "inventory") {
    val charges = player.inventory.charges(player, slot)
    player.message("Your Camulet has $charges ${"charge".plural(charges)} left.")
    if (charges == 0) {
        player.message("You can recharge it by applying camel dung.")
    }
}

itemOnItem("ugthanki_dung", "camulet") { player ->
    val slot = if (fromItem.id == "camulet") fromSlot else toSlot
    val charges = player.inventory.charges(player, slot)
    if (charges == 4) {
        player.message("Your Camulet already has 4 charges.")
        return@itemOnItem
    }
    if (player.inventory.replace("ugthanki_dung", "bucket")) {
        player.message("You recharge the Camulet using camel dung. Yuck!")
        player["camulet_charges"] = 4
    }
}