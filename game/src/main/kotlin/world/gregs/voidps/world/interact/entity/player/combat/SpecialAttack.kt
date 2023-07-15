package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.visual.update.player.EquipSlot
import kotlin.math.floor

const val MAX_SPECIAL_ATTACK = 1000

var Player.specialAttack: Boolean
    get() = get("special_attack", false)
    set(value) = set("special_attack", value)

var Player.specialAttackEnergy: Int
    get() = get("special_attack_energy", MAX_SPECIAL_ATTACK)
    set(value) {
        set("special_attack_energy", value)
        if (value < MAX_SPECIAL_ATTACK) {
            softTimers.startIfAbsent("restore_special_energy")
        }
    }

fun drainSpecialEnergy(player: Player, amount: Int): Boolean {
    var amount = amount
    if (player.equipped(EquipSlot.Ring).id == "ring_of_vigour") {
        amount = floor(amount * 0.9).toInt()
    }
    if (player.specialAttackEnergy < amount) {
        player.message("You don't have enough power left.")
        player.specialAttack = false
        return false
    }
    player.specialAttackEnergy -= amount
    return true
}