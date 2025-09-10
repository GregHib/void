package content.skill.melee.weapon.special

import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttackPrepare
import content.entity.sound.sound
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script

@Script
class DragonBattleaxe {

    init {
        specialAttackPrepare("rampage") { player ->
            cancel()
            if (!SpecialAttack.drain(player)) {
                return@specialAttackPrepare
            }
            player.anim("${id}_special")
            player.gfx("${id}_special")
            player.sound("${id}_special")
            player.say("Raarrrrrgggggghhhhhhh!")
            player.levels.drain(Skill.Attack, multiplier = 0.10)
            player.levels.drain(Skill.Defence, multiplier = 0.10)
            player.levels.drain(Skill.Magic, multiplier = 0.10)
            player.levels.drain(Skill.Ranged, multiplier = 0.10)
            player.levels.boost(Skill.Strength, amount = 5, multiplier = 0.15)
        }
    }
}
