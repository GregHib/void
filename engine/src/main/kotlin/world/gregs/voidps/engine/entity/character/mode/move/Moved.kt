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

fun move(filter: Moved.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend Moved.(Player) -> Unit) {
    on<Moved>(filter, priority, block)
}

fun move(filter: Moved.(NPC) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend Moved.(NPC) -> Unit) {
    on<Moved>(filter, priority, block)
}

fun move(filter: Moved.(Character) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend Moved.(Character) -> Unit) {
    on<Moved>(filter, priority, block)
}

fun move(filter: Moved.(Player) -> Boolean, block: Moved.() -> Unit) {
    on<Moved>(filter) { _: Player ->
        block.invoke(this)
    }
}

fun npcMove(filter: Moved.(NPC) -> Boolean, block: Moved.() -> Unit) {
    on<Moved>(filter) { _: NPC ->
        block.invoke(this)
    }
}

fun characterMove(filter: Moved.(Character) -> Boolean, block: Moved.() -> Unit) {
    on<Moved>(filter) { _: Character ->
        block.invoke(this)
    }
}