package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitEffectiveLevelOverride
import kotlin.math.floor

on<HitEffectiveLevelOverride>({ defence && it.hasEffect("prayer_turmoil") }, Priority.HIGH) { player: Player ->
    if (!player.getVar("turmoil", false)) {
        player.toggleVar("turmoil")
    }
    if (target != null) {
        player.setVar("turmoil_attack_bonus", floor(target.levels.get(Skill.Attack).coerceAtMost(99) * 0.15).toInt())
        player.setVar("turmoil_strength_bonus", floor(target.levels.get(Skill.Strength).coerceAtMost(99) * 0.10).toInt())
        player.setVar("turmoil_defence_bonus", floor(target.levels.get(Skill.Defence).coerceAtMost(99) * 0.15).toInt())
    }
}

on<HitEffectiveLevelOverride>({ defence && !it.hasEffect("prayer_turmoil") && it.getVar("turmoil", false) }, Priority.HIGH) { player: Player ->
    player.toggleVar("turmoil")
}