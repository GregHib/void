package world.gregs.voidps.world.community.chat

import world.gregs.voidps.engine.client.privateStatus
import world.gregs.voidps.engine.client.publicStatus
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<Registered> { player: Player ->
    player.privateStatus(player.privateStatus)
    player.publicStatus(player.publicStatus, player.tradeStatus)
    player.sendVar("game_status")
    player.sendVar("assist_status")
    player.sendVar("clan_status")
}

on<InterfaceOption>({ id == "filter_buttons" && component != "report" && component != "assist" && option != "View" }) { player: Player ->
    when (component) {
        "game", "clan" -> player.setVar("${component}_status", option.lowercase())
        "public" -> player.publicStatus = option.lowercase()
        "private" -> player.privateStatus = option.lowercase()
        "trade" -> player.tradeStatus = option.lowercase()
    }
}