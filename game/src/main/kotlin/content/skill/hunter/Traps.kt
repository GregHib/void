package content.skill.hunter

import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.entity.character.npc.NPC

object Traps {
    fun max(level: Int, max: Int) = (1 + level / 20).coerceAtMost(max)

    fun chance(npc: NPC, creature: RowDefinition): IntRange {
        var chance = creature.intRange("chance")
        // TODO do these combine?
        if (npc["baited", false]) {
            chance = (chance.first + 7)..(chance.last + 7) // 3%
        } else if (npc["smoked", false]) {
            chance = (chance.first + 5)..(chance.last + 5) // 2%
        }
        return chance
    }
}