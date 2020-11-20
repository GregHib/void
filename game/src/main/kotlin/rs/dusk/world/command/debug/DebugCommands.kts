import rs.dusk.engine.action.action
import rs.dusk.engine.client.send
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.ContainerItemsMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceTextMessage
import rs.dusk.utility.get
import rs.dusk.world.command.Command
import rs.dusk.world.interact.entity.obj.spawn.spawnObject

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

Command where { prefix == "obj" } then {
    if(content.isNotBlank()) {
        val parts = content.split(" ")
        val id = parts[0].toInt()
        val rotation = parts.getOrNull(1)?.toIntOrNull() ?: 0
        spawnObject(id, player.tile, 10, rotation, 10, null)
    } else {
        get<Objects>()[player.tile].forEach {
            println(it.id)
        }
    }
}

Command where { prefix == "tree" } then {
    val parts = content.split(" ")
    val tree = parts[0].toInt()
    val stump = parts[1].toInt()
    val type = parts.getOrNull(2)?.toIntOrNull() ?: 10
    player.action {
        spawnObject(tree, player.tile, type, 0, 5, null)
        delay(5)
        spawnObject(stump, player.tile, type, 0, 5, null)
    }
}