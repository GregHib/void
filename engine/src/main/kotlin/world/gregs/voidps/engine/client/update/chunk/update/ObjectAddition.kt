package world.gregs.voidps.engine.client.update.chunk.update

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.chunk.ChunkUpdate

data class ObjectAddition(val gameObject: GameObject) : ChunkUpdate(4) {
    override fun visible(player: Player) = gameObject.visible(player)
}