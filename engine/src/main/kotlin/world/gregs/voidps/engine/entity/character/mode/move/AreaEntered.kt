package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPC
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

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int): Any? = when (index) {
        0 -> "${dispatcher.key}_enter"
        1 -> name
        2 -> dispatcher.identifier
        3 -> tags
        4 -> area
        else -> null
    }
}

fun enterArea(area: String = "*", tag: String = "*", handler: suspend AreaEntered.() -> Unit) {
    Events.handle<Player, AreaEntered>("player_enter", area, "player", tag, "*") {
        handler.invoke(this)
    }
}

fun npcEnterArea(npc: String = "*", area: String = "*", tag: String = "*", handler: suspend AreaEntered.() -> Unit) {
    Events.handle<NPC, AreaEntered>("npc_enter", area, npc, tag, "*") {
        handler.invoke(this)
    }
}

fun characterEnterArea(area: String = "*", tag: String = "*", handler: suspend AreaEntered.() -> Unit) {
    val block: suspend AreaEntered.(EventDispatcher) -> Unit = {
        handler.invoke(this)
    }
    Events.handle("player_enter", area, "player", tag, "*", handler = block)
    Events.handle("npc_enter", area, "*", tag, "*", handler = block)
}