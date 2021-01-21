package world.gregs.void.engine.entity.proj

import kotlinx.coroutines.Job
import world.gregs.void.engine.entity.Entity
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.update.visual.player.name
import world.gregs.void.engine.map.Tile

/**
 * @author GregHib <greg@gregs.world>
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