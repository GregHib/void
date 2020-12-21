import rs.dusk.engine.action.action
import rs.dusk.engine.client.send
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.entity.definition.ObjectDefinitions
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.collision.CollisionFlag
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.map.collision.check
import rs.dusk.network.rs.codec.game.encode.message.ContainerItemsMessage
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
    val collisions: Collisions = get()
    println(collisions.check(player.tile.x, player.tile.y, player.tile.plane, CollisionFlag.BLOCKED))
}

Command where { prefix == "sendItems" } then {
    player.send(ContainerItemsMessage(90, IntArray(28) { 995 }, IntArray(28) { 1 }, false))
    player.send(ContainerItemsMessage(90, IntArray(28) { 11694 }, IntArray(28) { 1 }, true))
}

Command where { prefix == "obj" } then {
    if(content.isNotBlank()) {
        val parts = content.split(" ")
        val id = parts.getOrNull(0)?.toIntOrNull()
        if(id != null) {
            val rotation = parts.getOrNull(1)?.toIntOrNull() ?: 0
            spawnObject(id, player.tile, 10, rotation, 10, null)
        } else {
            val definitions = get<ObjectDefinitions>()
            val id = definitions.getId(content)
            if (id >= 0) {
                spawnObject(id, player.tile, 10, 0, 10, null)
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