package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.world.interact.entity.combat.hit.Damage
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.sound.areaSound

specialAttack("saradomins_lightning") { player ->
    player.setAnimation("${id}_special")
    areaSound("godwars_godsword_special_attack", player.tile)
    val weapon = player.weapon
    val damage = Damage.roll(player, target, "melee", weapon)
    player.hit(target, damage = damage)
    if (damage > 0) {
        areaSound("godwars_saradomin_magic_impact", target.tile, 10)
        player.hit(target, type = "magic")
    }
}