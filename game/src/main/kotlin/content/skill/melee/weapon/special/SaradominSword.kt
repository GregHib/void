package content.skill.melee.weapon.special

import content.entity.combat.hit.Damage
import content.entity.combat.hit.hit
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.areaSound

class SaradominSword : Script {

    init {
        specialAttack("saradomins_lightning") { target, id ->
            anim("${id}_special")
            areaSound("godwars_godsword_special_attack", tile)
            val weapon = weapon
            val damage = Damage.roll(this, target, "melee", weapon)
            hit(target, damage = damage)
            if (damage > 0) {
                target.gfx("saradomins_lightning_impact")
                areaSound("godwars_saradomin_magic_impact", target.tile, 10)
                hit(target, offensiveType = "magic")
            }
        }
    }
}
