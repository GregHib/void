package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.range.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.range.special.specialAttack

fun isGraniteMaul(weapon: Item?) = weapon != null && weapon.name.startsWith("granite_maul")

on<CombatSwing>({ !swung() && it.specialAttack && isGraniteMaul(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        return@on
    }
    player.setAnimation("quick_smash")
    player.setGraphic("quick_smash", height = 100)
    player.hit(target)
    delay = 1
}

on<VariableSet>({ key == "special_attack" && to == true && isGraniteMaul(it.weapon) }) { player: Player ->
    if (player.action.type != ActionType.Combat) {
        return@on
    }
    val target: Character? = player.getOrNull("target")
    if (target == null) {
        player.specialAttack = false
        return@on
    }
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        return@on
    }
    player.setAnimation("quick_smash")
    player.setGraphic("quick_smash", height = 100)
    player.hit(target)
    player.specialAttack = false
}