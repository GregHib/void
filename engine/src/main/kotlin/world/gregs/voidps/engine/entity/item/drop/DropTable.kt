package world.gregs.voidps.engine.entity.item.drop

import world.gregs.voidps.engine.entity.World
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
    val type: TableType,
    val roll: Int,
    val drops: List<Drop>,
    override val chance: Int
) : Drop {

    /**
     * Roll a drop from the table
     * @param maximumRoll overridable maximum roll for dynamic chances
     * @param list optional list to add the drop to
     * @param members whether members drops should be allowed or not
     * @param player the player for [ItemDrop.predicate]'s
     */
    fun role(maximumRoll: Int = -1, list: MutableList<ItemDrop> = mutableListOf(), members: Boolean = false, player: Player? = null): MutableList<ItemDrop> {
        collect(list, maximumRoll, members, player, random(maximumRoll))
        return list
    }

    fun random(maximum: Int): Int {
        return random.nextInt(0, if (roll <= 0 && maximum != -1) maximum else roll)
    }

    fun collect(list: MutableList<ItemDrop>, value: Int, members: Boolean, player: Player?, roll: Int = random(value)): Boolean {
        var count = 0
        for (drop in drops) {
            if (drop.chance == 0) {
                continue
            }
            if (drop is DropTable) {
                if (drop.chance != -1) {
                    count += drop.chance
                    if (roll >= count) {
                        continue
                    }
                }
                if (drop.collect(list, value, members, player) && type == TableType.First) {
                    return true
                }
            } else if (drop is ItemDrop) {
                if (drop.members && !members) {
                    continue
                }
                val predicate = drop.predicate
                if (player != null && predicate != null && !predicate(player)) {
                    continue
                }
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

    /**
     * Approximate chance of getting an item
     * Used for debugging
     */
    fun chance(id: String, total: Double = 1.0): Pair<ItemDrop, Double>? {
        for (drop in drops) {
            if (drop is DropTable) {
                val tableChance = tableChance(drop, total)
                return drop.chance(id, tableChance) ?: continue
            } else if (drop is ItemDrop && drop.id == id) {
                return drop to (roll / drop.chance.toDouble()) * total
            }
        }
        return null
    }

    private fun tableChance(drop: DropTable, total: Double = 1.0) =
        if (drop.type == TableType.All) total else if (drop.chance != -1) (roll / drop.chance.toDouble()) * total else total

    /**
     * Approximate chance of getting the item at [index]
     * Used for debugging
     */
    fun chance(index: Int, total: Double = tableChance(this)): Pair<ItemDrop, Double>? {
        for (i in drops.indices) {
            val drop = drops[i]
            if (drop is DropTable) {
                continue
            } else if (drop is ItemDrop && i == index) {
                return drop to (roll / drop.chance.toDouble()) * total
            }
        }
        return null
    }

    class Builder {
        private var type: TableType = TableType.First
        private var roll: Int? = null
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
            if (roll != null) {
                val total = drops.sumOf { if (it is ItemDrop && it.members == World.members) it.chance else 0 }
                check(total <= roll!!) { "Chances $total cannot exceed roll $roll." }
            }
            return DropTable(type, roll ?: 1, drops, chance)
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>): DropTable {
            val type = map["type"] as? TableType ?: TableType.First
            val roll = map["roll"] as? Int ?: 1
            val drops = map["drops"] as List<Drop>
            val chance = map["chance"] as? Int ?: -1
            return DropTable(type, roll, drops, chance)
        }
    }
}