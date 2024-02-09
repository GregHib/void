package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem

val areas: AreaDefinitions by inject()

val digSite = areas["dig_site_teleport"]

inventoryItem("Rub", "dig_site_pendant_#") {
    player.message("You rub the pendant...", ChatType.Filter)
    jewelleryTeleport(player, inventory, slot, digSite)
}