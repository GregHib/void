import rs.dusk.engine.client.send
import rs.dusk.engine.entity.item.offset
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.chunk.ChunkBatcher
import rs.dusk.network.rs.codec.game.encode.message.ObjectAddMessage
import rs.dusk.network.rs.codec.game.encode.message.ObjectRemoveMessage
import rs.dusk.utility.inject
import rs.dusk.world.command.Command

val objects: Objects by inject()
val batcher: ChunkBatcher by inject()

Command where { prefix == "spawn" } then {
    batcher.sendChunk(player, player.tile.chunk)
    val parts = content.split(" ")
    player.send(ObjectAddMessage(player.tile.offset(), parts[0].toInt(), parts.getOrNull(1)?.toInt() ?: 0, parts.getOrNull(2)?.toInt() ?: 0))
}

Command where { prefix == "despawn" } then {
    batcher.sendChunk(player, player.tile.chunk)
    val parts = content.split(" ")
    player.send(ObjectRemoveMessage(player.tile.offset(), parts.getOrNull(1)?.toInt() ?: 0, parts.getOrNull(2)?.toInt() ?: 0))
}

Command where { prefix == "get" } then {
    val obj = objects[player.tile.chunk]
    obj.filter { it.tile == player.tile }.forEach {
        println(it)
    }
}