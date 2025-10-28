package content.social.chat

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.privateStatus
import world.gregs.voidps.engine.client.publicStatus
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script

var Player.publicStatus: String
    get() = get("public_status", "on")
    set(value) {
        set("public_status", value)
        publicStatus(value, tradeStatus)
    }

var Player.privateStatus: String
    get() = get("private_status", "on")
    set(value) {
        set("private_status", value)
        privateStatus(value)
    }

var Player.tradeStatus: String
    get() = get("trade_status", "on")
    set(value) {
        set("trade_status", value)
        publicStatus(publicStatus, value)
    }

@Script
class ChatFilters : Api {

    init {
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
    }
}
