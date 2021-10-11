package world.gregs.voidps.engine.entity.obj

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.serializer.GameObjectBuilder
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
@JsonDeserialize(builder = GameObjectBuilder::class)
data class GameObject(
    override val id: Int,
    override var tile: Tile,
    val type: Int,
    val rotation: Int,
    @JsonIgnore
    val owner: String? = null
) : Entity {
    @get:JsonIgnore
    val def: ObjectDefinition
        get() = get<ObjectDefinitions>().get(id)

    @get:JsonIgnore
    val stringId: String
        get() = get<ObjectDefinitions>().getName(id)

    @get:JsonIgnore
    val size: Size by lazy {
        Size(
            if (rotation and 0x1 == 1) def.sizeY else def.sizeX,
            if (rotation and 0x1 == 1) def.sizeX else def.sizeY
        )
    }

    @JsonIgnore
    override val events: Events = Events(this)
    override val values: Values = Values()
    @JsonIgnore
    lateinit var interactTarget: TileTargetStrategy

    fun visible(player: Player) = owner == null || owner == player.name

    override fun equals(other: Any?): Boolean {
        if (other !is GameObject) {
            return false
        }
        return id == other.id && tile == other.tile
    }
}