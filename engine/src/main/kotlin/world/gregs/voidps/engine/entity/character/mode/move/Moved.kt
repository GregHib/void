package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.event.SuspendableEvent
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

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int): Any? = when (index) {
        0 -> "${dispatcher.key}_move"
        1 -> dispatcher.identifier
        2 -> from
        3 -> to
        else -> null
    }
}

fun move(from: Tile = Tile.EMPTY, to: Tile = Tile.EMPTY, handler: suspend Moved.(Player) -> Unit) {
    Events.handle("player_move", "player", if (from == Tile.EMPTY) "*" else from, if (to == Tile.EMPTY) "*" else to, handler = handler)
}

fun npcMove(npc: String = "*", from: Tile = Tile.EMPTY, to: Tile = Tile.EMPTY, handler: suspend Moved.(NPC) -> Unit) {
    Events.handle("npc_move", npc, if (from == Tile.EMPTY) "*" else from, if (to == Tile.EMPTY) "*" else to, handler = handler)
}

fun characterMove(from: Tile = Tile.EMPTY, to: Tile = Tile.EMPTY, handler: suspend Moved.(Character) -> Unit) {
    val fromTile: Any = if (from == Tile.EMPTY) "*" else from
    val toTile: Any = if (to == Tile.EMPTY) "*" else to
    Events.handle("player_move", "player", fromTile, toTile, handler = handler)
    Events.handle("npc_move", "*", fromTile, toTile, handler = handler)
}

fun move(filter: Moved.(Player) -> Boolean = { true }, handler: suspend Moved.(Player) -> Unit) {
    Events.handle<Player, Moved>("player_move", "player", "*", "*") {
        if (filter.invoke(this, it)) {
            handler.invoke(this, it)
        }
    }
}

fun npcMove(filter: Moved.(NPC) -> Boolean = { true }, handler: suspend Moved.(NPC) -> Unit) {
    Events.handle<NPC, Moved>("npc_move", "*", "*", "*") {
        if (filter.invoke(this, it)) {
            handler.invoke(this, it)
        }
    }
}

fun characterMove(filter: Moved.(Character) -> Boolean = { true }, block: suspend Moved.(Character) -> Unit) {
    val handler: suspend Moved.(Character) -> Unit = {
        if (filter.invoke(this, it)) {
            block.invoke(this, it)
        }
    }
    Events.handle("player_move", "player", "*", "*", handler = handler)
    Events.handle("npc_move", "*", "*", "*", handler = handler)
}