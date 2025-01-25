package content.skill.ranged.weapon.special

import world.gregs.voidps.world.interact.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.entity.sound.playSound

specialAttack("powershot") { player ->
    player.anim("bow_accurate")
    player.gfx("special_arrow_shoot")
    player.playSound("${id}_special")
    val time = player.shoot(id = "special_arrow", target = target)
    player.hit(target, delay = time)
}