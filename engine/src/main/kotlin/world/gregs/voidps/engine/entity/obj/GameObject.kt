package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile

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

    lateinit var def: ObjectDefinition
    override lateinit var size: Size

    override val events: Events = Events(this)
    override var values: Values? = null

    fun visible(player: Player) = owner == null || owner == player.name

    override fun equals(other: Any?): Boolean {
        if (other !is GameObject) {
            return false
        }
        return id == other.id && tile == other.tile
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + tile.hashCode()
        return result
    }
}