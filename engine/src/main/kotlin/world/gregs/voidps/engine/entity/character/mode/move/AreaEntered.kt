package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.TickSuspension
import world.gregs.voidps.type.Area

data class AreaEntered<C : Character>(
    override val character: C,
    val name: String,
    val tags: Set<String>,
    val area: Area
) : SuspendableEvent, SuspendableContext<C> {

    override var onCancel: (() -> Unit)? = null

    override val size = 5

    override suspend fun pause(ticks: Int) {
        TickSuspension.start(character, ticks)
    }

    override fun parameter(dispatcher: EventDispatcher, index: Int): Any? = when (index) {
        0 -> "${dispatcher.key}_enter"
        1 -> name
        2 -> dispatcher.identifier
        3 -> tags
        4 -> area
        else -> null
    }
}

fun enterArea(area: String = "*", tag: String = "*", handler: suspend AreaEntered<Player>.() -> Unit) {
    Events.handle<Player, AreaEntered<Player>>("player_enter", area, "player", tag, "*") {
        handler.invoke(this)
    }
}

fun npcEnterArea(npc: String = "*", area: String = "*", tag: String = "*", handler: suspend AreaEntered<NPC>.() -> Unit) {
    Events.handle<NPC, AreaEntered<NPC>>("npc_enter", area, npc, tag, "*") {
        handler.invoke(this)
    }
}

fun characterEnterArea(area: String = "*", tag: String = "*", handler: suspend AreaEntered<Character>.() -> Unit) {
    val block: suspend AreaEntered<Character>.(EventDispatcher) -> Unit = {
        handler.invoke(this)
    }
    Events.handle("player_enter", area, "player", tag, "*", handler = block)
    Events.handle("npc_enter", area, "*", tag, "*", handler = block)
}