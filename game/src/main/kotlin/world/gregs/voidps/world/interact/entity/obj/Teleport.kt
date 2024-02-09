package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals
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

fun teleportTakeOff(option: String = "*", vararg ids: String = arrayOf("*"), block: suspend Teleport.() -> Unit) {
    for (id in ids) {
        on<Teleport>({ takeoff && wildcardEquals(option, this.option) && wildcardEquals(id, obj.stringId) }) { _: Player ->
            block.invoke(this)
        }
    }
}

fun teleportLand(option: String = "*", id: String = "*", block: suspend Teleport.() -> Unit) {
    on<Teleport>({ land && wildcardEquals(option, this.option) && wildcardEquals(id, obj.stringId) }) { _: Player ->
        block.invoke(this)
    }
}