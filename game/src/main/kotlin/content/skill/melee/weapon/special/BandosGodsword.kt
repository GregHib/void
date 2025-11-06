package content.skill.melee.weapon.special

import content.entity.player.combat.special.SpecialAttack
import content.skill.melee.weapon.drainByDamage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class BandosGodsword : Script, SpecialAttack {
    init {
        specialAttackDamage("warstrike") { target, damage ->
            if (damage >= 0) {
                drainByDamage(target, damage, Skill.Defence, Skill.Strength, Skill.Prayer, Skill.Attack, Skill.Magic, Skill.Ranged)
            }
        }
    }
}
