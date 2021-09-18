package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialDamageMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

fun isRuneClaws(item: Item?) = item != null && item.name.startsWith("rune_claws")

specialAccuracyMultiplier(1.1, ::isRuneClaws)
specialDamageMultiplier(1.1, ::isRuneClaws)

on<CombatSwing>({ !swung() && it.specialAttack && isRuneClaws(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 4)) {
        delay = -1
        return@on
    }
    player.setAnimation("impale")
    player.setGraphic("impale")
    player.hit(target)
    delay = 5
}