package world.gregs.voidps.world.interact.entity.player.combat.special

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.hasOrStart
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.network.encode.message
import kotlin.math.floor

const val MAX_SPECIAL_ATTACK = 1000

var Player.specialAttack: Boolean
    get() = getVar("special_attack", false)
    set(value) = setVar("special_attack", value)

var Player.specialAttackEnergy: Int
    get() = getVar("special_attack_energy", MAX_SPECIAL_ATTACK)
    set(value) = setVar("special_attack_energy", value)

fun drainSpecialEnergy(player: Player, amount: Int): Boolean {
    var amount = amount
    if (player.equipped(EquipSlot.Ring).name == "ring_of_vigour") {
        amount = floor(amount * 0.9).toInt()
    }
    if (player.specialAttackEnergy < amount) {
        player.message("You don't have enough power left.")
        player.specialAttack = false
        return false
    }
    player.specialAttackEnergy -= amount
    player.hasOrStart("restore_special_energy")
    return true
}