package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAttack
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

specialAttack("brine_sabre") { player ->
    if (player.tile.region.id != 11924) {
        player.message("You can only use this special attack under water.")
        player.specialAttack = false
    }
}

combatSwing("brine_sabre", "melee", special = true) { player ->
    player.setAnimation("liquify")
    player.setGraphic("liquify")
    player.hit(target)
    delay = 4
}