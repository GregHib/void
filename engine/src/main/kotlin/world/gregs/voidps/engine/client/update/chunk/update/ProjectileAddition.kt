package world.gregs.voidps.engine.client.update.chunk.update

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.proj.Projectile
import world.gregs.voidps.engine.map.chunk.ChunkUpdate

data class ProjectileAddition(val projectile: Projectile) : ChunkUpdate(16) {
    override fun visible(player: Player): Boolean = projectile.visible(player)
}