package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitEffectiveLevelModifier
import world.gregs.voidps.world.interact.entity.combat.attackStyle

on<HitEffectiveLevelModifier>(priority = Priority.MEDIUM) { character: Character ->
    if (character is Player) {
        if ((skill == Skill.Attack || skill == Skill.Ranged) && character.attackStyle == "accurate") {
            level += 3
        } else if ((skill == Skill.Attack || skill == Skill.Strength || skill == Skill.Defence) && character.attackStyle == "controlled") {
            level += 1
        } else if (skill == Skill.Defence && (character.attackStyle == "defensive" || character.attackStyle == "long_range")) {
            level += 3
        } else if (skill == Skill.Strength && character.attackStyle == "aggressive") {
            level += 3
        }
    }
    level += 8
    if (character is NPC) {
        level += 1
    }
}