package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.Damage
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

specialAttack("slice_and_dice") { player ->
    player.setAnimation("${id}_special")
    player.gfx("${id}_special")

    val weapon = player.weapon
    var (hit1, hit2, hit3, hit4) = intArrayOf(0, 0, 0, 0)
    val maxHit = Damage.maximum(player, target, "melee", weapon)
    if (Hit.success(player, target, "melee", weapon, special = true)) {
        hit1 = random.nextInt(maxHit / 2, maxHit - 10)
        hit2 = hit1 / 2
        hit3 = hit2 / 2
        hit4 = hit3 + if (random.nextBoolean()) 10 else 0
    } else if (Hit.success(player, target, "melee", weapon, special = true)) {
        hit2 = random.nextDouble(maxHit * 0.375, maxHit * 0.875).toInt()
        hit3 = hit2 / 2
        hit4 = hit3 + if (random.nextBoolean()) 10 else 0
    } else if (Hit.success(player, target, "melee", weapon, special = true)) {
        hit3 = random.nextDouble(maxHit * 0.25, maxHit * 0.75).toInt()
        hit4 = hit3 + if (random.nextBoolean()) 10 else 0
    } else if (Hit.success(player, target, "melee", weapon, special = true)) {
        hit4 = random.nextDouble(maxHit * 0.25, maxHit * 1.25).toInt()
    } else {
        hit3 = if (random.nextBoolean()) 10 else 0
        hit4 = if (random.nextBoolean()) 10 else 0
    }

    player.hit(target, damage = hit1)
    player.hit(target, damage = hit2)
    player.hit(target, damage = hit3, delay = 30)
    player.hit(target, damage = hit4, delay = 30)
}