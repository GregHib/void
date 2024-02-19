package world.gregs.voidps.world.community.chat

import world.gregs.voidps.engine.client.privateStatus
import world.gregs.voidps.engine.client.publicStatus
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.playerSpawn

playerSpawn { player ->
    player.privateStatus(player.privateStatus)
    player.publicStatus(player.publicStatus, player.tradeStatus)
    player.sendVariable("game_status")
    player.sendVariable("assist_status")
    player.sendVariable("clan_status")
}

interfaceOption("View", id = "filter_buttons") {
    when (component) {
        "game", "clan" -> player["${component}_status"] = option.lowercase()
        "public" -> player.publicStatus = option.lowercase()
        "private" -> player.privateStatus = option.lowercase()
        "trade" -> player.tradeStatus = option.lowercase()
    }
}