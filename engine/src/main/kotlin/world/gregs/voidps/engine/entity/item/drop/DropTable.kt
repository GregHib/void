package world.gregs.voidps.engine.entity.item.drop

import kotlin.random.Random

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
    val type: TableType,
    val roll: Int,
    val drops: List<Drop>
) : Drop {

    fun role(maximumRoll: Int = -1, list: MutableList<ItemDrop> = mutableListOf()): MutableList<ItemDrop> {
        collect(list, maximumRoll, random(maximumRoll))
        return list
    }

    fun random(maximum: Int): Int {
        return Random.nextInt(0, if (roll <= 0 && maximum != -1) maximum else roll)
    }

    fun collect(list: MutableList<ItemDrop>, value: Int, roll: Int = random(value)): Boolean {
        var count = 0
        for (drop in drops) {
            if (drop is DropTable) {
                if (drop.collect(list, value) && type == TableType.First) {
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

    class Builder {
        private var type: TableType = TableType.First
        private var roll = 1
        private var chance: Int = 1
        private val drops = mutableListOf<Drop>()

        fun addDrop(drop: Drop): Builder {
            this.drops.add(drop)
            return this
        }

        fun withRoll(total: Int): Builder {
            this.roll = total
            return this
        }

        fun withType(type: TableType): Builder {
            this.type = type
            return this
        }

        fun withChance(chance: Int): Builder {
            this.chance = chance
            return this
        }

        fun build(): DropTable {
            val total = drops.sumOf { if (it is ItemDrop) it.chance else 0 }
            assert(total < roll) { "Chances $total cannot exceed roll $roll." }
            return DropTable(type, roll, drops)
        }
    }
}