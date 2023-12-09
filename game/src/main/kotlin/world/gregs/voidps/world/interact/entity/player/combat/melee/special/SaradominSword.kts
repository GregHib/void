package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.Damage
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

fun isSaradominSword(weapon: Item) = weapon.id.startsWith("saradomin_sword")

on<CombatSwing>({ !swung() && it.specialAttack && isSaradominSword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        delay = -1
        return@on
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

on<CombatHit>({ isSaradominSword(weapon) && special && type == "melee" }) { character: Character ->
    character.setGraphic("saradomins_lightning")
}