import world.gregs.void.engine.entity.item.offset
import world.gregs.void.engine.entity.obj.Objects
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.engine.map.chunk.ChunkBatcher
import world.gregs.void.network.codec.game.encode.ObjectAddEncoder
import world.gregs.void.network.codec.game.encode.ObjectRemoveEncoder
import world.gregs.void.utility.inject
import world.gregs.void.world.command.Command

val objects: Objects by inject()
val batcher: ChunkBatcher by inject()
val addEncoder: ObjectAddEncoder by inject()
val removeEncoder: ObjectRemoveEncoder by inject()

Command where { prefix == "spawn" } then {
    batcher.sendChunk(player, player.tile.chunk)
    val parts = content.split(" ")
    addEncoder.encode(player, player.tile.offset(), parts[0].toInt(), parts.getOrNull(1)?.toInt() ?: 0, parts.getOrNull(2)?.toInt() ?: 0)
}

Command where { prefix == "despawn" } then {
    batcher.sendChunk(player, player.tile.chunk)
    val parts = content.split(" ")
    removeEncoder.encode(player, player.tile.offset(), parts.getOrNull(1)?.toInt() ?: 0, parts.getOrNull(2)?.toInt() ?: 0)
}

Command where { prefix == "get" } then {
    val obj = objects[player.tile.chunk]
    obj.filter { it.tile == player.tile }.forEach {
        println(it)
    }
}