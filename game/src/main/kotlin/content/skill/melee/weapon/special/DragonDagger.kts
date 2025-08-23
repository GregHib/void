package content.skill.melee.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.player.combat.special.specialAttackDamage

specialAttack("puncture") { player ->
    player.hit(target)
}
