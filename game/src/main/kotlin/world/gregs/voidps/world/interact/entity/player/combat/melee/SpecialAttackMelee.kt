package world.gregs.voidps.world.interact.entity.player.combat.melee

import world.gregs.voidps.engine.client.update.task.viewport.spiral
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.combat.*
import kotlin.math.floor
import kotlin.random.Random
import kotlin.random.nextInt

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

fun multiTargetHit(check: CombatAttack.() -> Boolean, remaining: (target: Character) -> Int) {
    val players: Players by inject()
    val npcs: NPCs by inject()
    on<CombatAttack>({ !special && target.inMultiCombat && check() }) { player: Player ->
        val group = if (target is Player) players else npcs
        target.tile
            .spiral(1)
            .map { group[it] }
            .filterNot { it == target }
            .flatten()
            .take(remaining(target))
            .forEach {
                hit(player, it, Random.nextInt(0..damage), type, weapon, spell, special = true)
            }
    }
}

fun Character.drainByDamage(damage: Int, vararg skills: Skill) {
    if (damage == -1) {
        return
    }
    var drain = damage / 10
    if (drain > 0) {
        for (skill in skills) {
            val current = levels.get(skill)
            if (current <= 1) {
                continue
            }
            levels.drain(skill, drain)
            drain -= current
            if (drain <= 0) {
                break
            }
        }
    }
}