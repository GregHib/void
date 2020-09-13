import rs.dusk.engine.client.send
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.ContainerItemsMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceTextMessage
import rs.dusk.world.command.Command

IntVariable(1109, Variable.Type.VARBIT).register("one")
IntVariable(1112, Variable.Type.VARBIT).register("two")
IntVariable(1113, Variable.Type.VARBIT).register("three")
IntVariable(1241, Variable.Type.VARBIT).register("four")
IntVariable(1242, Variable.Type.VARBIT).register("five")
IntVariable(741, Variable.Type.VARBIT).register("six")
IntVariable(743, Variable.Type.VARBIT).register("seven")
IntVariable(744, Variable.Type.VARBIT).register("eight")

Command where { prefix == "test" } then {
    for(i in 0 until 55) {
        player.send(InterfaceTextMessage(667, i, i.toString()))
    }
//    player.setVar("one", -1)
//    player.setVar("two", 0)
//    player.setVar("three", 0)
//    player.setVar("four", 16750848)
//    player.setVar("five", 15439903)
//    player.setVar("six", -1)
//    player.setVar("seven", -1)
//    player.setVar("eight", 0)
//    var parent = 752
//    var index = 7
//    player.send(InterfaceOpenMessage(false, parent, index, 389))
//    player.send(ScriptMessage(570, "Gran Exchange Item Search"))
//    player.interfaces.sendText("trade_confirm", "status", "Are you sure you want to make this trade?")
}

Command where { prefix == "sendItems" } then {
    player.send(ContainerItemsMessage(90, IntArray(28) { 995 }, IntArray(28) { 1 }, false))
    player.send(ContainerItemsMessage(90, IntArray(28) { 11694 }, IntArray(28) { 1 }, true))
}