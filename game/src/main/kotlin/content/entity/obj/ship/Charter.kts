package content.entity.obj.ship

import content.quest.questCompleted
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.entity.character.player.Player

val locations = listOf(
    "catherby",
    "brimhaven",
    "port_khazard",
    "port_sarim",
)

interfaceOpen("charter_ship_map") { player ->
}

fun refresh(player: Player) {
    player.interfaces.sendVisibility("charter_ship_map", "mos_le_harmless", player.questCompleted("mos_le_harmless"))
    player.interfaces.sendVisibility("charter_ship_map", "shipyard", player.questCompleted("the_grand_tree"))
    player.interfaces.sendVisibility("charter_ship_map", "port_tyras", player.questCompleted("regicide"))
    player.interfaces.sendVisibility("charter_ship_map", "port_phasmatys", player.questCompleted("priest_in_peril"))
    player.interfaces.sendVisibility("charter_ship_map", "oo_glog", player.questCompleted("as_a_first_resort"))
    player.interfaces.sendVisibility("charter_ship_map", "crandor", false)
    player.interfaces.sendVisibility("charter_ship_map", "karamja", false)

    for (location in locations) {
        player.interfaces.sendVisibility("charter_ship_map", location, true)
    }
}