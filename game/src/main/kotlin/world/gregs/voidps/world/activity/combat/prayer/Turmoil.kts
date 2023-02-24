package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.client.variable.toggle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitEffectiveLevelOverride
import kotlin.math.floor

on<HitEffectiveLevelOverride>({ defence && it.prayerActive("turmoil") }, Priority.HIGH) { player: Player ->
    if (!player.getVar("turmoil", false)) {
        player.toggle("turmoil")
    }
    if (target != null) {
        player.set("turmoil_attack_bonus", floor(target.levels.get(Skill.Attack).coerceAtMost(99) * 0.15).toInt())
        player.set("turmoil_strength_bonus", floor(target.levels.get(Skill.Strength).coerceAtMost(99) * 0.10).toInt())
        player.set("turmoil_defence_bonus", floor(target.levels.get(Skill.Defence).coerceAtMost(99) * 0.15).toInt())
    }
}

on<HitEffectiveLevelOverride>({ defence && !it.prayerActive("turmoil") && it.getVar("turmoil", false) }, Priority.HIGH) { player: Player ->
    player.toggle("turmoil")
}