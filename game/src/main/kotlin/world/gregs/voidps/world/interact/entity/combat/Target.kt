package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
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
            val range = Wilderness.combatRange(source)
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