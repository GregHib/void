package world.gregs.voidps.world.interact.entity.player.combat.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.weapon
import kotlin.math.floor

const val MAX_SPECIAL_ATTACK = 1000

data class SpecialAttack(val id: String, val target: Character) : Event {
    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "special_attack"
        1 -> id
        else -> null
    }

    companion object {
        fun hasEnergy(player: Player) = drain(player, drain = false)

        fun drain(player: Player, drain: Boolean = true): Boolean {
            val amount: Int? = player.weapon.def.getOrNull("special_energy")
            if (amount == null) {
                player.message("This weapon does not have a special attack.")
                player.specialAttack = false
                return false
            }
            var energy = amount
            if (player.equipped(EquipSlot.Ring).id == "ring_of_vigour") {
                energy = floor(energy * 0.9).toInt()
            }
            if (player.specialAttackEnergy < energy) {
                player.message("You don't have enough power left.")
                player.specialAttack = false
                return false
            }
            if (drain) {
                player.specialAttackEnergy -= energy
            }
            return true
        }
    }
}

fun specialAttack(id: String = "*", handler: suspend SpecialAttack.(Player) -> Unit) {
    Events.handle("special_attack", id, handler = handler)
}