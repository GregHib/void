package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.event.addEvent

data class Approach<T : Any>(val target: T, val option: String, val partial: Boolean) : SuspendableEvent()

@JvmName("onApproachPlayer")
inline fun <reified T : Any> onApproach(
    noinline condition: Approach<T>.(Player) -> Boolean = { true },
    priority: Priority = Priority.MEDIUM,
    noinline block: suspend Approach<T>.(player: Player, target: T) -> Unit
) = addEvent(condition, priority) { block.invoke(this, it, target) }

@JvmName("onApproachNPC")
inline fun <reified T : Any> onApproach(
    noinline condition: Approach<T>.(NPC) -> Boolean = { true },
    priority: Priority = Priority.MEDIUM,
    noinline block: suspend Approach<T>.(npc: NPC, target: T) -> Unit
) = addEvent(condition, priority) { block.invoke(this, it, target) }