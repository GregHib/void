package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject
import content.entity.player.inv.inventoryItem

val areas: AreaDefinitions by inject()

val digSite = areas["dig_site_teleport"]

inventoryItem("Rub", "dig_site_pendant_#") {
    player.message("You rub the pendant...", ChatType.Filter)
    jewelleryTeleport(player, inventory, slot, digSite)
}