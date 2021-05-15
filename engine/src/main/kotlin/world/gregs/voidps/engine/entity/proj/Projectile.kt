package world.gregs.voidps.engine.entity.proj

import kotlinx.coroutines.Job
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile

/**
 * @author GregHib <greg@gregs.world>
 * @since March 28, 2020
 */
data class Projectile(
    override val id: Int,
    override var tile: Tile,
    val direction: Delta,
    val index: Int,
    var delay: Int,
    var flightTime: Int,
    val startHeight: Int,
    val endHeight: Int,
    val curve: Int,
    val offset: Int,
    val owner: String? = null
) : Entity {

    override val events: Events = Events(this)
    override val values: Values = Values()
    var job: Job? = null

    fun visible(player: Player) = owner == null || owner == player.name
}