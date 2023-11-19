package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

fun isBrineSabre(item: Item?) = item != null && item.id == "brine_sabre"

specialAccuracyMultiplier(2.0, ::isBrineSabre)

on<VariableSet>({ key == "special_attack" && to == true && isBrineSabre(it.weapon) }) { player: Player ->
    if (player.tile.region.id != 11924) {
        player.message("You can only use this special attack under water.")
        player.specialAttack = false
    }
}

on<CombatSwing>({ !swung() && it.specialAttack && isBrineSabre(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, 750)) {
        delay = -1
        return@on
    }
    player.setAnimation("liquify")
    player.setGraphic("liquify")
    player.hit(target)
    delay = 4
}