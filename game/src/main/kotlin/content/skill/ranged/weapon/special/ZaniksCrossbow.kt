package content.skill.ranged.weapon.special

import content.entity.combat.hit.hit
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class ZaniksCrossbow : Script {

    init {
        specialAttack("defiance") { target, _ ->
            anim("zaniks_crossbow_special")
            gfx("zaniks_crossbow_special")
            val time = shoot(id = "zaniks_crossbow_bolt", target = target)
            val damage = hit(target, delay = time)
            if (damage != -1) {
                target.levels.drain(Skill.Defence, damage / 10)
            }
        }
    }
}
