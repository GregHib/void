package world.gregs.voidps.world.activity.transport

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val areas: AreaDefinitions by inject()

val digSite = areas["dig_site_teleport"]

on<InventoryOption>({ item.id.startsWith("dig_site_pendant_") && option == "Rub" }) { player: Player ->
    player.message("You rub the pendant...", ChatType.Filter)
    jewelleryTeleport(player, inventory, slot, digSite)
}