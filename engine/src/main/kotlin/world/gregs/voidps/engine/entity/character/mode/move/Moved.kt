package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.Tile

/**
 * Entity moved between [from] and [to] tiles
 */
data class Moved(
    override val character: Character,
    val from: Tile,
    val to: Tile
) : CancellableEvent(), CharacterContext, SuspendableEvent {
    override var onCancel: (() -> Unit)? = null
}

@JvmName("movePlayer")
fun move(filter: Moved.(Player) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend Moved.(Player) -> Unit) {
    on<Moved>(filter, priority, block)
}

@JvmName("moveNPC")
fun move(filter: Moved.(NPC) -> Boolean = { true}, priority: Priority = Priority.MEDIUM, block: suspend Moved.(NPC) -> Unit) {
    on<Moved>(filter, priority, block)
}

@JvmName("moveCharacter")
fun move(filter: Moved.(Character) -> Boolean = { true}, priority: Priority = Priority.MEDIUM, block: suspend Moved.(Character) -> Unit) {
    on<Moved>(filter, priority, block)
}