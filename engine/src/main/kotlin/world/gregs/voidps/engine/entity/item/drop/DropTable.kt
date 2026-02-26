package world.gregs.voidps.engine.entity.item.drop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random

/**
 * Distributes a collection of items to award for a monster kill.
 * [ItemDrop]'s are selected based on a single [random] roll between 0 and [roll]
 * Going down the [drops] list if the accumulated [ItemDrop.chance] exceeds the [roll] then that item is awarded.
 *
 * @param type The selection strategy; All - Every top-level drop is rolled, First - Only the first rewarded item is returned
 * @param roll The maximum roll (exclusive)
 * @param drops A list of [ItemDrop]'s and nested [DropTable]'s
 */
data class DropTable(
    val type: TableType = TableType.First,
    val roll: Int = 1,
    val drops: List<Drop>,
    override val chance: Int = -1,
    override val predicate: ((Player) -> Boolean)? = null,
) : Drop {

    /**
     * Roll a drop from the table
     * @param maximumRoll overridable maximum roll for dynamic chances
     * @param list optional list to add the drop to
     * @param player the player for [ItemDrop.predicate]'s
     */
    fun roll(maximumRoll: Int = -1, list: MutableList<ItemDrop> = mutableListOf(), player: Player? = null): MutableList<ItemDrop> {
        collect(list, maximumRoll, player, random(maximumRoll))
        return list
    }

    fun random(maximum: Int): Int = random.nextInt(0, if (roll <= 0 && maximum != -1) maximum else roll)

    fun collect(list: MutableList<ItemDrop>, value: Int, player: Player?, roll: Int = random(value)): Boolean {
        var count = 0
        for (drop in drops) {
            if (drop.chance == 0) {
                continue
            }
            val predicate = drop.predicate
            if (player != null && predicate != null && !predicate(player)) {
                continue
            }
            if (drop is DropTable) {
                if (drop.chance != -1) {
                    count += drop.chance
                    if (roll >= count) {
                        continue
                    }
                }
                if (drop.collect(list, value, player) && type == TableType.First) {
                    return true
                }
            } else if (drop is ItemDrop) {
                if (type == TableType.All) {
                    list.add(drop)
                } else {
                    count += drop.chance
                    if (roll < count) {
                        list.add(drop)
                        return true
                    }
                }
            }
        }
        return type == TableType.All
    }
}
