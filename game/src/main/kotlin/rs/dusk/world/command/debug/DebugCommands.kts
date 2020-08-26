import rs.dusk.engine.client.send
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage
import rs.dusk.world.command.Command
import rs.dusk.world.community.trade.Trade.sendContainerOptions

Command where { prefix == "test" } then {
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
    //Self
    sendContainerOptions(player, 335, 31, 90, false, 4, 7, arrayOf("Remove", "Remove-5", "Remove-10", "Remove-All", "Remove-X", "Value"))
    player.interfaces.sendSettings("trade_main", "offer_options", 0, 27, 0, 1, 2, 3, 4, 5, 9)
    // Other
    sendContainerOptions(player, 335, 34, 90, true, 4, 7, arrayOf("Value<col=FF9040>"))
    player.interfaces.sendSettings("trade_main", "other_options", 0, 27, 0, 9)
    // Side
    sendContainerOptions(player, 336, 0, 93, false, 4, 7, arrayOf("Offer", "Offer-5", "Offer-10", "Offer-All", "Offer-X", "Value", "Lend"))
    player.interfaces.sendSettings("trade_side", "offer", 0, 27, 0, 1, 2, 3, 4, 5, 6, 9)

//    sendFlash(player, 335, 33, 4, 7, 0)
//    sendFlash(player, 336, 0, 4, 7, 0)
//    player.setVar("other_trader_name", "Bob")
}

fun sendFlash(player: Player, id: Int, component: Int, width: Int, height: Int, slot: Int) {
    player.send(ScriptMessage(143, (id shl 16) or component, width, height, slot))
}