package world.gregs.voidps.world.interact.entity.player.combat.melee

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import kotlin.random.nextInt

fun multiTargetHit(check: CombatAttack.() -> Boolean, remaining: (target: Character) -> Int) {
    val players: Players by inject()
    val npcs: NPCs by inject()
    on<CombatAttack>({ !special && target.inMultiCombat && check() }) { player: Player ->
        val group = if (target is Player) players else npcs
        var hit = 0
        val hits = remaining(target)
        for (tile in target.tile.spiral(1)) {
            val characters = group[tile]
            if (characters == target) {
                continue
            }
            for (character in characters) {
                if (hit >= hits) {
                    return@on
                }
                hit++
                character.directHit(player, random.nextInt(0..damage), type, weapon, spell, special = true)
            }
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