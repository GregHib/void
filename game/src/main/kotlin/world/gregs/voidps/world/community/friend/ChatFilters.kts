import world.gregs.voidps.engine.client.privateStatus
import world.gregs.voidps.engine.client.publicStatus
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.community.friend.privateStatus
import world.gregs.voidps.world.community.friend.publicStatus
import world.gregs.voidps.world.community.friend.tradeStatus

on<Registered> { player: Player ->
    player.privateStatus(player.privateStatus)
    player.publicStatus(player.publicStatus, player.tradeStatus)
    player.sendVar("game_status")
    player.sendVar("assist_status")
    player.sendVar("clan_status")
}

on<InterfaceOption>({ id == "filter_buttons" && component != "report" && option != "View" }) { player: Player ->
    when (component) {
        "game", "clan", "assist" -> player.setVar("${component}_status", if (option.startsWith("off", true)) "off" else option.lowercase())
        "public" -> player.publicStatus = option.lowercase()
        "private" -> player.privateStatus = option.lowercase()
        "trade" -> player.tradeStatus = option.lowercase()
    }
}