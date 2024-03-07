package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.type.Area

data class AreaExited(
    override val character: Character,
    val name: String,
    val tags: Set<String>,
    val area: Area
) : SuspendableEvent, CharacterContext {
    override var onCancel: (() -> Unit)? = null

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int): Any? = when (index) {
        0 -> "${dispatcher.key}_exit"
        1 -> name
        2 -> dispatcher.identifier
        3 -> tags
        4 -> area
        else -> null
    }
}

fun exitArea(area: String = "*", tag: String = "*", override: Boolean = true, block: suspend AreaExited.() -> Unit) {
    Events.handle<Player, AreaExited>("player_exit", area, "player", tag, "*", override = override) {
        block.invoke(this)
    }
}

fun npcExitArea(npc: String = "*", area: String = "*", tag: String = "*", override: Boolean = true, block: suspend AreaExited.() -> Unit) {
    Events.handle<Player, AreaExited>("npc_exit", area, npc, tag, "*", override = override) {
        block.invoke(this)
    }
}