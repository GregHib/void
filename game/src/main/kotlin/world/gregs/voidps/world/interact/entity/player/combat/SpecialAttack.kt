package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy

data class SpecialAttack(val id: String, val target: Character) : Event {
    override fun size() = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_special_attack"
        1 -> id
        2 -> target.identifier
        else -> null
    }
}

fun specialAttackPrepare(weapon: String, block: suspend VariableSet.(Player) -> Unit) {
    variableSet("special_attack", to = true) { player ->
        if (from != true && wildcardEquals(weapon, player.weapon.id)) {
            val energy: Int = player.weapon.def.getOrNull("special_energy") ?: return@variableSet
            if (!drainSpecialEnergy(player, energy)) {
                return@variableSet
            }
            block.invoke(this, player)
        }
    }
}