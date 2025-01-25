package content.skill.ranged.combat.weapon.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot

specialAttack("defiance") { player ->
    player.anim("zaniks_crossbow_special")
    player.gfx("zaniks_crossbow_special")
    val time = player.shoot(id = "zaniks_crossbow_bolt", target = target)
    val damage = player.hit(target, delay = time)
    if (damage != -1) {
        target.levels.drain(Skill.Defence, damage / 10)
    }
}