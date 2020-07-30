package rs.dusk.engine.model.entity.character.player

import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage

/**
 * @param player Unconnected player save
 */
data class PlayerSpawn(
    val player: Player,
    val name: String,
    val session: Session? = null,
    val data: GameLoginMessage? = null
) : Event<Player>() {
    companion object : EventCompanion<PlayerSpawn>
}