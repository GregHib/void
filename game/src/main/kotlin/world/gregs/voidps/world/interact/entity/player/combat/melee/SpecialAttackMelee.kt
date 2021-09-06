package world.gregs.voidps.world.interact.entity.player.combat.melee

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.HitRatingModifier
import kotlin.math.floor

fun specialDamageMultiplier(multiplier: Double, check: (Item) -> Boolean) {
    on<HitDamageModifier>({ type == "melee" && special && weapon != null && check(weapon) }, Priority.HIGH) { _: Player ->
        damage = floor(damage * multiplier)
    }
}
fun specialAccuracyMultiplier(multiplier: Double, check: (Item) -> Boolean) {
    on<HitRatingModifier>({ offense && type == "melee" && special && weapon != null && check(weapon) }, Priority.HIGH) { _: Player ->
        rating = floor(rating * multiplier)
    }
}

fun specialDrainByDamage(check: (Item) -> Boolean, vararg skills: Skill) {
    on<CombatHit>({ weapon != null && special && check(weapon) }) { character: Character ->
        var drain = damage / 10
        if (drain > 0) {
            for (skill in skills) {
                val current = character.levels.get(skill)
                if (current <= 1) {
                    continue
                }
                character.levels.drain(skill, drain)
                drain -= current
                if (drain <= 0) {
                    break
                }
            }
        }
    }
}