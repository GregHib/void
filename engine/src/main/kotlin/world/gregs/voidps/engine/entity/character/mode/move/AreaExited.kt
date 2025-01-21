package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Area

data class AreaExited<C : Character>(
    override val character: C,
    val name: String,
    val tags: Set<String>,
    val area: Area
) : SuspendableEvent, SuspendableContext<C> {
    override var onCancel: (() -> Unit)? = null

    override val size = 5

    override suspend fun pause(ticks: Int) {
        Suspension.start(character, ticks)
    }

    override fun parameter(dispatcher: EventDispatcher, index: Int): Any? = when (index) {
        0 -> "${dispatcher.key}_exit"
        1 -> name
        2 -> dispatcher.identifier
        3 -> tags
        4 -> area
        else -> null
    }
}

fun exitArea(area: String = "*", tag: String = "*", handler: suspend AreaExited<Player>.() -> Unit) {
    Events.handle<Player, AreaExited<Player>>("player_exit", area, "player", tag, "*") {
        handler.invoke(this)
    }
}

fun npcExitArea(npc: String = "*", area: String = "*", tag: String = "*", handler: suspend AreaExited<NPC>.() -> Unit) {
    Events.handle<NPC, AreaExited<NPC>>("npc_exit", area, npc, tag, "*") {
        handler.invoke(this)
    }
}

fun characterExitArea(area: String = "*", tag: String = "*", handler: suspend AreaExited<Character>.() -> Unit) {
    val block: suspend AreaExited<Character>.(EventDispatcher) -> Unit = {
        handler.invoke(this)
    }
    Events.handle("player_exit", area, "player", tag, "*", handler = block)
    Events.handle("npc_exit", area, "*", tag, "*", handler = block)
}