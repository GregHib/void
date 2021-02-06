import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.map.collision.get
import world.gregs.voidps.network.codec.game.encode.InterfaceColourEncoder
import world.gregs.voidps.network.codec.game.encode.InterfaceVisibilityEncoder
import world.gregs.voidps.network.codec.game.encode.sendContainerItems
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.command.Command
import world.gregs.voidps.world.interact.entity.obj.spawn.spawnObject

IntVariable(1109, Variable.Type.VARBIT).register("one")
IntVariable(1112, Variable.Type.VARBIT).register("two")
IntVariable(1113, Variable.Type.VARBIT).register("three")
IntVariable(1241, Variable.Type.VARBIT).register("four")
IntVariable(1242, Variable.Type.VARBIT).register("five")
IntVariable(741, Variable.Type.VARBIT).register("six")
IntVariable(743, Variable.Type.VARBIT).register("seven")
IntVariable(744, Variable.Type.VARBIT).register("eight")

Command where { prefix == "test" } then {
    val encoder: InterfaceColourEncoder = get()
    encoder.encode(player, 746, 226, 255, 0, 0)
    val encoder2: InterfaceVisibilityEncoder = get()
    encoder2.encode(player, 746, 226, true)
    println("Sent")
}

Command where { prefix == "sendItems" } then {
    player.sendContainerItems(90, IntArray(28) { 995 }, IntArray(28) { 1 }, false)
    player.sendContainerItems(90, IntArray(28) { 11694 }, IntArray(28) { 1 }, true)
}

Command where { prefix == "obj" } then {
    if(content.isNotBlank()) {
        val parts = content.split(" ")
        val id = parts.getOrNull(0)?.toIntOrNull()
        val type = 10
        if(id != null) {
            val rotation = parts.getOrNull(1)?.toIntOrNull() ?: 0
            spawnObject(id, player.tile.add(y = 2), 10, rotation, 10, null)
            spawnObject(id, player.tile.add(y = 2), 22, rotation, 10, null)
        } else {
            val definitions = get<ObjectDefinitions>()
            val id = definitions.getId(content)
            if (id >= 0) {
                spawnObject(id, player.tile, type, 0, 10, null)
            }
        }
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