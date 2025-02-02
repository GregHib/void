package content.area.wilderness

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel

object Wilderness {
    fun combatRange(player: Player): IntRange {
        var wildernessLevel = 0
        if (player.tile.x in 3008..3135 && player.tile.y in 9920..10367) {
            wildernessLevel = (player.tile.y - 9920) / 8 + 1
        } else if (player.tile.x in 2944..3392 && player.tile.y in 3525..3967 && !player["decrease_combat_attack_range", false]) {
            wildernessLevel = (player.tile.y - 3520) / 8 + 1
        }
        wildernessLevel = wildernessLevel.coerceIn(0..60)
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

val Character.inPvp: Boolean
    get() = get("in_pvp", false)

val Character.inWilderness: Boolean
    get() = get("in_wilderness", false)

val Character.inMultiCombat: Boolean
    get() = contains("in_multi_combat")

val Character.inSingleCombat: Boolean
    get() = !inMultiCombat