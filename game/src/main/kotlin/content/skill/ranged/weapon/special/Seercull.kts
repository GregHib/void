package content.skill.ranged.weapon.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.characterLevelChange
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.entity.sound.playSound

specialAttack("soulshot") { player ->
    player.anim("bow_accurate")
    player.gfx("seercull_special_shoot")
    player.playSound("seercull_special")
    val time = player.shoot(id = "seercull_special_arrow", target = target)
    player.hit(target, delay = time)
}

characterCombatHit("seercull", "range") { character ->
    character.gfx("seercull_special_hit")
}

combatAttack("seercull*") {
    if (target["soulshot", false] || !special) {
        return@combatAttack
    }
    target["soulshot"] = true
    target.levels.drain(Skill.Magic, damage / 10)
}

characterLevelChange(Skill.Magic) { character ->
    if (character["soulshot", false] && to >= character.levels.getMax(skill)) {
        character.clear("soulshot")
    }
}