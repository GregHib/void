package content.skill.ranged.weapon.special

import content.entity.combat.hit.characterCombatDamage
import content.entity.combat.hit.combatAttack
import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound

class Seercull : Script, SpecialAttack {
    init {
        specialAttack("soulshot") { target, _ ->
            anim("bow_accurate")
            gfx("seercull_special_shoot")
            sound("seercull_special")
            val time = shoot(id = "seercull_special_arrow", target = target)
            hit(target, delay = time)
        }

        characterCombatDamage("seercull", "range") { character ->
            character.gfx("seercull_special_impact")
        }

        combatAttack("seercull*") {
            if (target["soulshot", false] || !special) {
                return@combatAttack
            }
            target["soulshot"] = true
            target.levels.drain(Skill.Magic, damage / 10)
        }

        npcLevelChanged(Skill.Magic) { skill, _, to ->
            if (get("soulshot", false) && to >= levels.getMax(skill)) {
                clear("soulshot")
            }
        }
    }
}
