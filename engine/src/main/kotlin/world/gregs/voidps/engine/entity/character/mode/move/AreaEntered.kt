package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.type.Area

data class AreaEntered(
    override val character: Character,
    val name: String,
    val tags: Set<String>,
    val area: Area
) : SuspendableEvent, CharacterContext {
    override var onCancel: (() -> Unit)? = null

    override fun size() = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int): Any? = when (index) {
        0 -> "${dispatcher.key}_enter"
        1 -> name
        2 -> dispatcher.identifier
        3 -> tags
        4 -> area
        else -> null
    }
}

fun enterArea(area: String = "*", tag: String = "*", override: Boolean = true, block: suspend AreaEntered.() -> Unit) {
    Events.handle<Player, AreaEntered>("player_enter", area, "player", tag, "*", override = override) {
        block.invoke(this)
    }
}

fun npcEnterArea(npc: String = "*", area: String = "*", tag: String = "*", override: Boolean = true, block: suspend AreaEntered.() -> Unit) {
    Events.handle<Player, AreaEntered>("npc_enter", area, npc, tag, "*", override = override) {
        block.invoke(this)
    }
}