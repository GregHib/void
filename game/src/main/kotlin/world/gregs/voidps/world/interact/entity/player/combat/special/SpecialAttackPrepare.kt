package world.gregs.voidps.world.interact.entity.player.combat.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class SpecialAttackPrepare(val id: String) : CancellableEvent() {
    override fun size() = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "special_attack_prepare"
        1 -> id
        else -> null
    }
}

fun specialAttackPrepare(id: String, override: Boolean = true, block: suspend SpecialAttackPrepare.(Player) -> Unit) {
    Events.handle("special_attack_prepare", id, override = override, handler = block)
}