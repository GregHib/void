package rs.dusk.engine.entity.obj

import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.player.name
import rs.dusk.engine.entity.definition.ObjectDefinitions
import rs.dusk.engine.entity.item.offset
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.chunk.ChunkBatcher
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.network.codec.game.encode.ObjectAnimationSpecificEncoder
import rs.dusk.utility.get

/**
 * Interactive Object
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class GameObject(
    override val id: Int,
    override var tile: Tile,
    val type: Int,
    val rotation: Int,
    val owner: String? = null
) : Entity {
    val def: ObjectDefinition
        get() = get<ObjectDefinitions>().get(id)
    val stringId: String
        get() = get<ObjectDefinitions>().getName(id)

    val size: Size by lazy {
        Size(
            if (rotation and 0x1 == 1) def.sizeY else def.sizeX,
            if (rotation and 0x1 == 1) def.sizeX else def.sizeY
        )
    }

    lateinit var interactTarget: TargetStrategy

    fun visible(player: Player) = owner == null || owner == player.name
}

fun GameObject.animate(id: Int) {
    val encoder: ObjectAnimationSpecificEncoder = get()
    get<ChunkBatcher>().update(tile.chunk) { player -> encoder.encode(player, tile.offset(), id, type, rotation) }
}