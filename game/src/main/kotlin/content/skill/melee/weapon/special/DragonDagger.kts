package content.skill.melee.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttackDamage

specialAttackDamage("puncture", noHit = false) { player ->
    player.hit(target)
}
