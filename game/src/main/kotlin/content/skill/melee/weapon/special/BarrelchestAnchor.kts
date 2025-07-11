package content.skill.melee.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.sound.sound
import content.skill.melee.weapon.drainByDamage
import world.gregs.voidps.engine.entity.character.player.skill.Skill

specialAttack("sunder") { player ->
    player.anim("${id}_special")
    player.gfx("${id}_special")
    player.sound("${id}_special")
    val damage = player.hit(target, delay = 60)
    if (damage >= 0) {
        drainByDamage(target, damage, Skill.Defence, Skill.Strength, Skill.Prayer, Skill.Attack, Skill.Magic, Skill.Ranged)
    }
}
