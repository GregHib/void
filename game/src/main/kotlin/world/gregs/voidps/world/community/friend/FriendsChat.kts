package world.gregs.voidps.world.community.friend

import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

on(InterfaceOption) {
    where {
        name == "friends_chat" && component == "settings" && option == "Open Settings"
    }
    then {
        if(player.hasScreenOpen()) {
            player.message("Please close the interface you have open before using Friends Chat setup.")
            return@then
        }
        player.open("friends_chat_setup")
    }
}