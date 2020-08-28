import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.entity.character.contain.detail.ContainerDetails
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage
import rs.dusk.utility.get
import rs.dusk.world.command.Command

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
    player.interfaceOptions.send("trade_main", "offer_options")
    player.interfaceOptions.unlockAll("trade_main", "offer_options", 0 until 28)
    // Other
    player.interfaceOptions.send("trade_main", "other_options")
    player.interfaceOptions.unlockAll("trade_main", "other_options", 0 until 28)
    // Side
    player.interfaceOptions.send("trade_side", "offer")
    player.interfaceOptions.unlockAll("trade_side", "offer", 0 until 28)

//    sendFlash(player, 335, 33, 4, 7, 0)
//    sendFlash(player, 336, 0, 4, 7, 0)
//    player.setVar("other_trader_name", "Bob")
}

fun Player.highlight(name: String, component: String, slot: Int) {
    val details: InterfaceDetails = get()
    val containerDetails: ContainerDetails = get()
    val comp = details.getComponent(name, component)
    val container = containerDetails.get(comp.container)
    send(ScriptMessage(143, (comp.parent shl 16) or comp.id, container.width, container.height, slot))
}