package content.skill.melee.weapon.special

import content.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class DragonHatchet : Script, SpecialAttack {
    init {
        specialAttackDamage("clobber") { target, damage ->
            if (damage < 0) {
                return@specialAttackDamage
            }
            val drain = damage / 100
            if (drain > 0) {
                target.levels.drain(Skill.Defence, drain)
                target.levels.drain(Skill.Magic, drain)
            }
        }
    }
}
