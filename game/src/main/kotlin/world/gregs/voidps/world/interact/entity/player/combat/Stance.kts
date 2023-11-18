package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitEffectiveLevelModifier
import world.gregs.voidps.world.interact.entity.combat.attackStyle

on<HitEffectiveLevelModifier>(priority = Priority.MEDIUM) { player: Player ->
    level += when {
        (skill == Skill.Attack || skill == Skill.Ranged) && player.attackStyle == "accurate" -> 3
        (skill == Skill.Attack || skill == Skill.Strength || skill == Skill.Defence) && player.attackStyle == "controlled" -> 1
        skill == Skill.Defence && (player.attackStyle == "defensive" || player.attackStyle == "long_range") -> 3
        skill == Skill.Strength && player.attackStyle == "aggressive" -> 3
        skill == Skill.Magic -> 1
        else -> 0
    }
}

on<HitEffectiveLevelModifier>(priority = Priority.MEDIUM) { _: Character ->
    level += 8
}

on<HitEffectiveLevelModifier>(priority = Priority.MEDIUM) { _: NPC ->
    level += 1
}