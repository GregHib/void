package content.social.chat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.privateStatus
import world.gregs.voidps.engine.client.publicStatus
import world.gregs.voidps.engine.entity.character.player.Player

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

class ChatFilters : Script {

    init {
        playerSpawn {
            privateStatus(privateStatus)
            publicStatus(publicStatus, tradeStatus)
            sendVariable("game_status")
            sendVariable("assist_status")
            sendVariable("clan_status")
        }

        interfaceOption("View", id = "filter_buttons:*") {
            when (it.component) {
                "game", "clan" -> set("${it.component}_status", it.option.lowercase())
                "public" -> publicStatus = it.option.lowercase()
                "private" -> privateStatus = it.option.lowercase()
                "trade" -> tradeStatus = it.option.lowercase()
            }
        }
    }
}
