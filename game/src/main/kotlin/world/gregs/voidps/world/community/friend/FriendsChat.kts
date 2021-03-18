package world.gregs.voidps.world.community.friend

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.encode.message

InterfaceOption where { name == "friends_chat" && component == "settings" && option == "Open Settings" } then {
    if (player.hasScreenOpen()) {
        player.message("Please close the interface you have open before using Friends Chat setup.")
        return@then
    }
    player.open("friends_chat_setup")
}