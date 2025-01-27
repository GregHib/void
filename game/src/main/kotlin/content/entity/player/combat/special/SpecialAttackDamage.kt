package content.entity.player.combat.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class SpecialAttackDamage(
    val id: String,
    val target: Character,
    val damage: Int
) : Event {
    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "special_attack_damage"
        1 -> id
        2 -> damage >= 0
        else -> null
    }
}

fun specialAttackDamage(id: String, noHit: Boolean = true, handler: suspend SpecialAttackDamage.(Player) -> Unit) {
    Events.handle("special_attack_damage", id, if (noHit) true else "*", handler = handler)
}