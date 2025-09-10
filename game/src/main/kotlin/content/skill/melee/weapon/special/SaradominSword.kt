package content.skill.melee.weapon.special

import content.entity.combat.hit.Damage
import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.sound.areaSound
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.event.Script
@Script
class SaradominSword {

    init {
        specialAttack("saradomins_lightning") { player ->
            player.anim("${id}_special")
            areaSound("godwars_godsword_special_attack", player.tile)
            val weapon = player.weapon
            val damage = Damage.roll(player, target, "melee", weapon)
            player.hit(target, damage = damage)
            if (damage > 0) {
                target.gfx("saradomins_lightning_impact")
                areaSound("godwars_saradomin_magic_impact", target.tile, 10)
                player.hit(target, offensiveType = "magic")
            }
        }

    }

}
