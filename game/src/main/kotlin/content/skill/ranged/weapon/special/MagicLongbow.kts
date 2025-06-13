package content.skill.ranged.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.entity.sound.sound

specialAttack("powershot") { player ->
    player.anim("bow_accurate")
    player.gfx("special_arrow_shoot")
    player.sound("${id}_special")
    val time = player.shoot(id = "special_arrow", target = target)
    player.hit(target, delay = time)
}
