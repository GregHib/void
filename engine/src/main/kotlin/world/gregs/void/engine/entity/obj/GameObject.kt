package world.gregs.void.engine.entity.obj

import world.gregs.void.cache.definition.data.ObjectDefinition
import world.gregs.void.engine.entity.Entity
import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.update.visual.player.name
import world.gregs.void.engine.entity.definition.ObjectDefinitions
import world.gregs.void.engine.entity.item.offset
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.map.chunk.ChunkBatcher
import world.gregs.void.engine.path.TargetStrategy
import world.gregs.void.network.codec.game.encode.ObjectAnimationSpecificEncoder
import world.gregs.void.utility.get

/**
 * Interactive Object
 * @author GregHib <greg@gregs.world>
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