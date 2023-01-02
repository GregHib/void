package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.InteractiveEntity
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.event.addEvent

data class Operated<T : InteractiveEntity>(val target: T, val option: String, val partial: Boolean) : SuspendableEvent()

@JvmName("onOperatePlayer")
inline fun <reified T : InteractiveEntity> onOperate(
    noinline condition: Operated<T>.(Player) -> Boolean = { true },
    priority: Priority = Priority.MEDIUM,
    noinline block: suspend Operated<T>.(player: Player, target: T) -> Unit
) = addEvent(condition, priority) { block.invoke(this, it, target) }

@JvmName("onOperateNPC")
inline fun <reified T : InteractiveEntity> onOperate(
    noinline condition: Operated<T>.(NPC) -> Boolean = { true },
    priority: Priority = Priority.MEDIUM,
    noinline block: suspend Operated<T>.(npc: NPC, target: T) -> Unit
) = addEvent(condition, priority) { block.invoke(this, it, target) }