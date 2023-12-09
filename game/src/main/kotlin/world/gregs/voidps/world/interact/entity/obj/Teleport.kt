package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.type.Tile

data class Teleport(
    override val character: Character,
    val id: String,
    val tile: Tile,
    val obj: ObjectDefinition,
    val option: String
) : CancellableEvent(), CharacterContext {
    var delay: Int? = null
    override var onCancel: (() -> Unit)? = null
    var land: Boolean = false
    val takeoff: Boolean
        get() = !land
}