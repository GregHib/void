package content.skill.ranged.weapon.special

import content.entity.combat.hit.characterCombatDamage
import content.entity.combat.hit.combatAttack
import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.entity.sound.sound
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.SkillId
import world.gregs.voidps.engine.event.Script

@Script
class Seercull : Api {
    init {
        specialAttack("soulshot") { player ->
            player.anim("bow_accurate")
            player.gfx("seercull_special_shoot")
            player.sound("seercull_special")
            val time = player.shoot(id = "seercull_special_arrow", target = target)
            player.hit(target, delay = time)
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
    }

    @SkillId(Skill.Magic)
    override fun levelChanged(npc: NPC, skill: Skill, from: Int, to: Int) {
        if (npc["soulshot", false] && to >= npc.levels.getMax(skill)) {
            npc.clear("soulshot")
        }
    }
}
