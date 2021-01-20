package world.gregs.void.engine.entity.character.player

import io.netty.channel.Channel
import world.gregs.void.engine.event.Event
import world.gregs.void.engine.event.EventCompanion

/**
 * @param player Unconnected player save
 */
data class PlayerSpawn(
    val player: Player,
    val name: String,
    val session: Channel? = null,
    val data: GameLoginInfo? = null
) : Event<Player>() {
    companion object : EventCompanion<PlayerSpawn>
}