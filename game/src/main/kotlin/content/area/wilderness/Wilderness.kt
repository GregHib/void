package content.area.wilderness

import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel

object Wilderness {
    fun combatRange(player: Player): IntRange {
        val wildernessLevel = player.wildernessLevel
        val combatLevel = player.combatLevel
        val min = (combatLevel - (wildernessLevel + (5 + combatLevel / 10))).coerceAtLeast(20)
        var max = (combatLevel + (wildernessLevel + (5 + combatLevel / 10))).coerceAtMost(138)
        while (max < 139 && max - (wildernessLevel + (5 + max / 10)) <= combatLevel) {
            max += 1
        }
        max -= 1
        return min..max
    }
}

val Character.wildernessLevel: Int
    get() {
        var wildernessLevel = 0
        if (tile.x in 3008..3135 && tile.y in 9920..10367) {
            wildernessLevel = (tile.y - 9920) / 8 + 1
        } else if (tile.x in 2944..3392 && tile.y in 3525..3967 && !get("decrease_combat_attack_range", false)) {
            wildernessLevel = (tile.y - 3520) / 8 + 1
        }
        return wildernessLevel.coerceIn(0..60)
    }

val Character.inPvp: Boolean
    get() = get("in_pvp", false)

val Character.inWilderness: Boolean
    get() = get("in_wilderness", false)

/**
 * True when wilderness-style PvP consequences apply: combat-start skull and full item drops on
 * death. Wilderness itself, plus the Clan Wars FFA dangerous arena. Distinct from [inPvp], which
 * only gates "can you attack here" and is also true in safer arenas (FFA safe, etc.).
 */
val Character.inFullPvp: Boolean
    get() = inWilderness || tile in Areas["clan_wars_ffa_dangerous_arena"]

val Character.inMultiCombat: Boolean
    get() = contains("in_multi_combat")

val Character.inSingleCombat: Boolean
    get() = !inMultiCombat
