package rs.dusk.engine.entity.character.player

import rs.dusk.core.network.connection.Session
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

/**
 * @param player Unconnected player save
 */
data class PlayerSpawn(
    val player: Player,
    val name: String,
    val session: Session? = null,
    val data: GameLoginInfo? = null
) : Event<Player>() {
    companion object : EventCompanion<PlayerSpawn>
}