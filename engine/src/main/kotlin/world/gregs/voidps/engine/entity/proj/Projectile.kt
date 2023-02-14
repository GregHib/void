package world.gregs.voidps.engine.entity.proj

import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timer.Timer

/**
 * @param id Projectile graphic id
 * @param direction The delta between start and end x & y coordinates
 * @param index Target index plus one, negated for player
 * @param delay time before starting in client ticks, 30 = 1 tick
 * @param flightTime time to reach target in client ticks, 30 = 1 tick
 * @param startHeight 40 = player head height
 * @param endHeight 40 = player head height
 * @param curve value between -63..63
 * @param offset offset from start coordinate, 64 = 1 tile
 */
data class Projectile(
    val id: String,
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

    override val size: Size = Size.ONE
    override val events: Events = Events(this)
    override var values: Values? = null
    var timer: Timer? = null

    fun visible(player: Player) = owner == null || owner == player.name

    lateinit var def: GraphicDefinition
}