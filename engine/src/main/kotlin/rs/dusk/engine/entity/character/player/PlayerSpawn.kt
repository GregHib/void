package rs.dusk.engine.entity.character.player

import io.netty.channel.Channel
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

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