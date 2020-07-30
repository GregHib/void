package rs.dusk.engine.model.entity.proj

import kotlinx.coroutines.Job
import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.update.visual.player.name
import rs.dusk.engine.model.map.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Projectile(
    override val id: Int,
    override var tile: Tile,
    val direction: Tile,
    val index: Int,
    var delay: Int,
    var flightTime: Int,
    val startHeight: Int,
    val endHeight: Int,
    val curve: Int,
    val offset: Int,
    val owner: String? = null
) : Entity {

    var job: Job? = null

    fun visible(player: Player) = owner == null || owner == player.name
}