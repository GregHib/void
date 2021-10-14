package world.gregs.voidps.engine.entity.sound

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile

data class AreaSound(
    override var tile: Tile,
    val intId: Int,
    val radius: Int,
    val repeat: Int,
    val delay: Int,
    val volume: Int,
    val speed: Int,
    val midi: Boolean,
    val owner: String? = null
) : Entity {

    fun visible(player: Player) = owner == null || owner == player.name
    override val events: Events = Events(this)
    override val values: Values = Values()
}