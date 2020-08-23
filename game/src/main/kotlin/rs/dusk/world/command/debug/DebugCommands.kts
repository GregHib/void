import rs.dusk.engine.client.send
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage
import rs.dusk.world.command.Command

Command where { prefix == "test" } then {

    //Self
    player.send(
        ScriptMessage(
            150, "", "", "", "Value<col=FF9040>", "Remove-X", "Remove-All", "Remove-10",
            "Remove-5", "Remove", -1, 0, 7, 4, 90,
            335 shl 16 or 31
        )
    )
    player.interfaces.sendSetting(335, 31, 0, 27, 1150)
    // Other
    player.send(
        ScriptMessage(
            695, "", "", "", "", "", "", "", "", "Value<col=FF9040>", -1, 0, 7, 4, 90,
            335 shl 16 or 34
        )
    )
    player.interfaces.sendSetting(335, 34, 0, 27, 1026)
    // Side
    player.send(
        ScriptMessage(
            150, "", "", "Lend", "Value<col=FF9040>", "Offer-X", "Offer-All", "Offer-10",
            "Offer-5", "Offer", -1, 0, 7, 4, 93,
            336 shl 16
        )
    )
    player.interfaces.sendSetting(336, 0, 0, 27, 1278)

    sendFlash(player, 335, 33, 4, 7, 0)
    player.interfaces.apply {
        open("trade_main")
        open("trade_side")
        sendText("trade_main", "status", "This is a status update")
        sendText("trade_main", "title", "Trading with: Someone")
        sendText("trade_main", "slots", "Many free inventory slots.")
//        sendText("trade_main", "loanTime", "1 hour")
//        sendVisibility("trade_main", "loan", true)
//        sendSetting("trade_main", "loan", 0, 1, 264190)
    }
    player.setVar("other_trader_name", "Bob")
}

fun sendFlash(player: Player, id: Int, component: Int, width: Int, height: Int, slot: Int) {
    player.send(
        ScriptMessage(
            143, slot, height, width, (id shl 16) or component
        )
    )
}