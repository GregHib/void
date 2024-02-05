package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.variable.specialAttack
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.underAttack
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

fun isGraniteMaul(weapon: Item?) = weapon != null && weapon.id.startsWith("granite_maul")

combatSwing({ !swung() && it.specialAttack && isGraniteMaul(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@combatSwing
    }
    player.setAnimation("quick_smash")
    player.setGraphic("quick_smash")
    player.hit(target)
    delay = 1
}

specialAttack("granite_maul*") { player: Player ->
    if (!player.underAttack) {
        return@specialAttack
    }
    val target: Character? = player["target"]
    if (target == null) {
        player.specialAttack = false
        return@specialAttack
    }
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        return@specialAttack
    }
    player.setAnimation("quick_smash")
    player.setGraphic("quick_smash")
    player.hit(target)
    player.specialAttack = false
}