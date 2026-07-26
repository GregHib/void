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

    val statuses = mapOf(
        "game_status" to listOf("all", "filter"),
        "public_status" to listOf("on", "friends", "off", "hide"),
        "private_status" to listOf("on", "friends", "off"),
        "trade_status" to listOf("on", "friends", "off"),
        "clan_status" to listOf("on", "friends", "off"),
        "assist_status" to listOf("on", "friends", "off"),
    )

    init {
        playerSpawn {
            // Left-clicking a filter button used to store its "View" option as a status,
            // leaving an invalid value that hides the filter's messages every session
            for ((key, values) in statuses) {
                if (get(key, values.first()) !in values) {
                    clear(key)
                }
            }
            privateStatus(privateStatus)
            publicStatus(publicStatus, tradeStatus)
        }

        for (option in listOf("On", "Friends", "Off")) {
            interfaceOption(option, "filter_buttons:clan") {
                set("clan_status", option.lowercase())
            }
            interfaceOption(option, "filter_buttons:trade") {
                tradeStatus = option.lowercase()
            }
        }
        for (option in listOf("On", "Friends", "Off", "Hide")) {
            interfaceOption(option, "filter_buttons:public") {
                publicStatus = option.lowercase()
            }
        }
        interfaceOption("All", "filter_buttons:game") {
            set("game_status", "all")
        }
        interfaceOption("Filter", "filter_buttons:game") {
            set("game_status", "filter")
        }
    }
}
