package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.entity.player.equip.inventory

val areas: AreaDefinitions by inject()

val edgeville = areas["edgeville_teleport"]
val karamja = areas["karamja_teleport"]
val draynorVillage = areas["draynor_village_teleport"]
val alKharid = areas["al_kharid_teleport"]

inventory({ inventory == "inventory" && item.id.startsWith("amulet_of_glory_") && option == "Rub" }) { player: Player ->
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

inventory({ inventory == "worn_equipment" && item.id.startsWith("amulet_of_glory_") }) { player: Player ->
    val area = when (option) {
        "Edgeville" -> edgeville
        "Karamja" -> karamja
        "Draynor Village" -> draynorVillage
        "Al Kharid" -> alKharid
        else -> return@inventory
    }
    player.message("You rub the amulet...", ChatType.Filter)
    jewelleryTeleport(player, inventory, slot, area)
}