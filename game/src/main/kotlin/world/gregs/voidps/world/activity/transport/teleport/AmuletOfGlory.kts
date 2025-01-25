package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject
import content.entity.player.dialogue.type.choice
import content.entity.player.inv.inventoryItem

val areas: AreaDefinitions by inject()

val edgeville = areas["edgeville_teleport"]
val karamja = areas["karamja_teleport"]
val draynorVillage = areas["draynor_village_teleport"]
val alKharid = areas["al_kharid_teleport"]

inventoryItem("Rub", "amulet_of_glory_#", "inventory") {
    choice("Where would you like to teleport to?") {
        option("Edgeville") {
            player.message("You rub the amulet...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, edgeville)
        }
        option("Karamja") {
            player.message("You rub the amulet...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, karamja)
        }
        option("Draynor Village") {
            player.message("You rub the amulet...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, draynorVillage)
        }
        option("Al Kharid") {
            player.message("You rub the amulet...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, alKharid)
        }
        option("Nowhere")
    }
}

inventoryItem("*", "amulet_of_glory_#", "worn_equipment") {
    val area = when (option) {
        "Edgeville" -> edgeville
        "Karamja" -> karamja
        "Draynor Village" -> draynorVillage
        "Al Kharid" -> alKharid
        else -> return@inventoryItem
    }
    player.message("You rub the amulet...", ChatType.Filter)
    jewelleryTeleport(player, inventory, slot, area)
}