package world.gregs.voidps.engine.entity.obj

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.serializer.GameObjectBuilder
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.network.codec.game.encode.ObjectAnimationSpecificEncoder
import world.gregs.voidps.utility.get

/**
 * Interactive Object
 * @author GregHib <greg@gregs.world>
 * @since March 28, 2020
 */
@JsonDeserialize(builder = GameObjectBuilder::class)
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

    lateinit var interactTarget: TileTargetStrategy

    fun visible(player: Player) = owner == null || owner == player.name
}

fun GameObject.animate(id: Int) {
    val encoder: ObjectAnimationSpecificEncoder = get()
    get<ChunkBatcher>().update(tile.chunk) { player -> encoder.encode(player, tile.offset(), id, type, rotation) }
}