import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.EffectiveLevelModifier
import world.gregs.voidps.world.interact.entity.combat.attackStyle

on<EffectiveLevelModifier>(priority = Priority.MEDIUM) { player: Player ->
    if ((skill == Skill.Attack || skill == Skill.Range) && player.attackStyle == "accurate") {
        level += 3
    } else if ((skill == Skill.Attack || skill == Skill.Strength || skill == Skill.Defence) && player.attackStyle == "controlled") {
        level += 1
    } else if (skill == Skill.Defence && (player.attackStyle == "defensive" || player.attackStyle == "long_range")) {
        level += 3
    } else if (skill == Skill.Strength && player.attackStyle == "aggressive") {
        level += 3
    }
    level += 8
}

on<EffectiveLevelModifier>(priority = Priority.MEDIUM) { _: NPC ->
    level += 8
}