package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.get

object Target {
    fun attackable(source: Character, target: Character): Boolean {
        if (target is NPC) {
            if (target.def.options[1] != "Attack") {
                return false
            }
            if (get<NPCs>().indexed(target.index) == null) {
                return false
            }
        }
        if (source.tile.level != target.tile.level) {
            return false
        }
        if (source.dead || target.dead) {
            return false
        }
        if (source is Player && target is Player) {
            if (!source.inWilderness) {
                source.message("You can only attack players in a player-vs-player area.")
                return false
            }
            if (!target.inWilderness) {
                source.message("That player is not in the wilderness.")
                return false
            }
            val range = combatRange(source)
            if (target.combatLevel !in range) {
                source.message("Your level difference is too great!")
                source.message("You need to move deeper into the Wilderness.")
                return false
            }
        }
        if (target.inSingleCombat && target.underAttack && !target.attackers.contains(source)) {
            if (target is NPC) {
                (source as? Player)?.message("Someone else is fighting that.")
            } else {
                (source as? Player)?.message("That player is already under attack.")
            }
            return false
        }
        if (source.inSingleCombat && source.underAttack && !source.attackers.contains(target)) {
            (source as? Player)?.message("You are already in combat.")
            return false
        }
        // PVP area, slayer requirements, in combat etc..
        return true
    }

    private fun combatRange(player: Player): IntRange {
        var diff = 0
        if (player.tile.x in 3008..3135 && player.tile.y in 9920..10367) {
            diff = (player.tile.y - 9920) / 8 + 1
        } else if (player.tile.x in 2944..3392 && player.tile.y in 3525..3967 && player["decrease_combat_attack_range", false]) {
            diff = (player.tile.y - 3520) / 8 + 1
        }
        diff = diff.coerceIn(0..60)
        val combatLevel = player.combatLevel
        val min = (combatLevel - (diff + (5 + combatLevel / 10))).coerceAtLeast(20)
        var max = (combatLevel + (diff + (5 + combatLevel / 10))).coerceAtMost(138)
        while (max < 139 && max - (diff + (5 + max / 10)) <= combatLevel) {
            max += 1
        }
        max -= 1
        return min..max
    }
}

internal var Character.target: Character?
    get() = getOrNull("target")
    set(value) {
        if (value != null) {
            set("target", value)
        } else {
            clear("target")
        }
    }
val Character.underAttack: Boolean
    get() = hasClock("under_attack")