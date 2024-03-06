package world.gregs.voidps.world.interact.entity.player.combat.melee

import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

combatPrepare("melee") { player ->
    val amount: Int? = player.weapon.def.getOrNull("special_energy")
    if (player.specialAttack && amount != null && !drainSpecialEnergy(player, amount)) {
        player.specialAttack = false
        cancel()
    }
}