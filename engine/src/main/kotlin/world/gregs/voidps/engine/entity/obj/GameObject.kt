package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.utility.get

/**
 * Interactive Object
 */
data class GameObject(
    val id: String,
    override var tile: Tile,
    val type: Int,
    val rotation: Int,
    val owner: String? = null
) : Entity {

    val def: ObjectDefinition
        get() = get<ObjectDefinitions>().get(id)

    val size: Size by lazy {
        Size(
            if (rotation and 0x1 == 1) def.sizeY else def.sizeX,
            if (rotation and 0x1 == 1) def.sizeX else def.sizeY
        )
    }

    override val events: Events = Events(this)
    override val values: Values = Values()
    lateinit var interactTarget: TileTargetStrategy

    fun visible(player: Player) = owner == null || owner == player.name

    override fun equals(other: Any?): Boolean {
        if (other !is GameObject) {
            return false
        }
        return id == other.id && tile == other.tile
    }
}