package rs.dusk.engine.model.entity.obj

import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.update.visual.player.name
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.get

/**
 * Interactive Object
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Location(
    override val id: Int,
    override var tile: Tile,
    val size: Size,
    val type: Int,
    val rotation: Int,
    val owner: String? = null
) : Entity {
    val def: ObjectDefinition
        get() = get<ObjectDecoder>().getSafe(id)

    fun visible(player: Player) = owner == null || owner == player.name
}