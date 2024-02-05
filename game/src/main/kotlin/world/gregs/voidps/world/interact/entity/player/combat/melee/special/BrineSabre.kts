package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

fun isBrineSabre(item: Item) = item.id == "brine_sabre"

variableSet({ key == "special_attack" && to == true && isBrineSabre(it.weapon) }) { player: Player ->
    if (player.tile.region.id != 11924) {
        player.message("You can only use this special attack under water.")
        player.specialAttack = false
    }
}

combatSwing({ !swung() && it.specialAttack && isBrineSabre(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, 750)) {
        delay = -1
        return@combatSwing
    }
    player.setAnimation("liquify")
    player.setGraphic("liquify")
    player.hit(target)
    delay = 4
}