package world.gregs.voidps.engine.entity.sound

import world.gregs.voidps.engine.data.definition.config.SoundDefinition
import world.gregs.voidps.engine.data.definition.extra.SoundDefinitions
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile

data class AreaSound(
    override var tile: Tile,
    val id: String,
    val radius: Int,
    val repeat: Int,
    val delay: Int,
    val volume: Int,
    val speed: Int,
    val midi: Boolean,
    val owner: String? = null
) : Entity {

    override val size: Size = Size.ONE
    override val events: Events = Events(this)
    override var values: Values? = null

    val def: SoundDefinition
        get() = get<SoundDefinitions>().get(id)
}