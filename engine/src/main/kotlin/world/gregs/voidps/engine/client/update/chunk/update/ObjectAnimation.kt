package world.gregs.voidps.engine.client.update.chunk.update

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.chunk.ChunkUpdate
import world.gregs.voidps.engine.utility.get

data class ObjectAnimation(val id: Int, val gameObject: GameObject) : ChunkUpdate(4) {
    override fun visible(player: Player) = true
}

fun GameObject.animate(id: Int) = get<ChunkBatches>().update(tile.chunk, ObjectAnimation(id, this))