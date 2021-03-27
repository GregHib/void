import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.network.encode.addObject
import world.gregs.voidps.network.encode.removeObject
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.utility.inject

val objects: Objects by inject()
val batcher: ChunkBatcher by inject()

on<Command>({ prefix == "spawn" }) { player: Player ->
    batcher.sendChunk(player, player.tile.chunk)
    val parts = content.split(" ")
    player.client?.addObject(player.tile.offset(), parts[0].toInt(), parts.getOrNull(1)?.toInt() ?: 0, parts.getOrNull(2)?.toInt() ?: 0)
}

on<Command>({ prefix == "despawn" }) { player: Player ->
    batcher.sendChunk(player, player.tile.chunk)
    val parts = content.split(" ")
    player.client?.removeObject(player.tile.offset(), parts.getOrNull(1)?.toInt() ?: 0, parts.getOrNull(2)?.toInt() ?: 0)
}

on<Command>({ prefix == "get" }) { player: Player ->
    val obj = objects[player.tile.chunk]
    obj.filter { it.tile == player.tile }.forEach {
        println(it)
    }
}