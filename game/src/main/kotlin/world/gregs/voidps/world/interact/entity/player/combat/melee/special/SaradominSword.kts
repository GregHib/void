package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Damage
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy

combatSwing("saradomin_sword*", "melee", special = true) { player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        delay = -1
        return@combatSwing
    }
    player.setAnimation("saradomins_lightning")
    val weapon = player.weapon
    val damage = Damage.roll(player, target, "melee", weapon)
    player.hit(target, damage = damage)
    if (damage > 0) {
        player.hit(target, type = "magic")
    }
    delay = 4
}

characterCombatHit("saradomin_sword*", "melee", special = true) { character ->
    character.setGraphic("saradomins_lightning")
}