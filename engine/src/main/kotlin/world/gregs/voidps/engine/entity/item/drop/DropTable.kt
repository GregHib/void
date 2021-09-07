package world.gregs.voidps.engine.entity.item.drop

import kotlin.random.Random

data class DropTable(
    val type: TableType,
    val roll: Int,
    val drops: List<Drop>,
    override val chance: Int
) : Drop {

    fun role(list: MutableList<ItemDrop> = mutableListOf(), value: Int = -1): List<ItemDrop> {
        val roll = Random.nextInt(0, if (roll == 0 && value != -1) value else roll)
        var count = 0
        for (drop in drops) {
            count += drop.chance
            if (roll < count) {
                when (drop) {
                    is DropTable -> drop.role(list, value)
                    is ItemDrop -> list.add(drop)
                }
                if (type == TableType.First) {
                    return list
                }
            }
        }
        return list
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
            val total = drops.sumOf { it.chance }
            assert(total < roll) { "Chances $total cannot exceed roll $roll." }
            return DropTable(type, roll, drops, chance)
        }
    }
}