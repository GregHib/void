package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.melee.drainByDamage
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.sound.playSound

specialAttack("sunder") { player ->
    player.setAnimation("${id}_special")
    player.gfx("${id}_special")
    player.playSound("${id}_special")
    val damage = player.hit(target, delay = 60)
    if (damage >= 0) {
        drainByDamage(target, damage, Skill.Defence, Skill.Strength, Skill.Prayer, Skill.Attack, Skill.Magic, Skill.Ranged)
    }
}