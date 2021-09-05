package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialDamageMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import kotlin.math.floor

fun isArmadylGodsword(weapon: Item?) = weapon != null && weapon.name.startsWith("armadyl_godsword")

on<HitDamageModifier>({ type == "melee" && special && isArmadylGodsword(weapon) }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.25)
}

specialDamageMultiplier(1.1, ::isArmadylGodsword)
specialAccuracyMultiplier(2.0, ::isArmadylGodsword)

on<CombatSwing>({ !swung() && it.specialAttack && isArmadylGodsword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.setAnimation("the_judgement")
    player.setGraphic("the_judgement")
    player.hit(target)
    delay = 6
}