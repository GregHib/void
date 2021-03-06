package world.gregs.voidps.engine.entity.character.player.login

import world.gregs.voidps.engine.entity.character.player.GameLoginInfo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventCompanion
import world.gregs.voidps.network.ClientSession

/**
 * @param player Unconnected player save
 */
data class PlayerSpawn(
    val player: Player,
    val name: String,
    val session: ClientSession? = null,
    val data: GameLoginInfo? = null
) : Event<Player>() {
    companion object : EventCompanion<PlayerSpawn>
}