package world.gregs.voidps.engine.entity.item.drop

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DropTableTest {

    @Test
    fun `Roll number from 0 until drop table roll value`() {
        val drops = DropTable(TableType.First, 100, listOf())
        val roll: Int = drops.random(maximum = Int.MAX_VALUE)
        assertTrue(roll in 0 until 100)
    }

    @Test
    fun `Roll number from 0 until value passed in`() {
        val drops = DropTable(TableType.First, 0, listOf())
        val roll: Int = drops.random(maximum = 100)
        assertTrue(roll in 0 until 100)
    }

    @Test
    fun `Roll every item in all type table`() {
        val item1 = drop("1", 1)
        val item2 = drop("2", 1)
        val root = DropTable(TableType.All, -1, listOf(item1, item2))

        val list = mutableListOf<ItemDrop>()
        root.collect(list, -1, -1)

        assertTrue(list.contains(item1))
        assertTrue(list.contains(item2))
    }

    @Test
    fun `Roll first item in table of tables`() {
        val item1 = drop("1", 1)
        val item2 = drop("2", 1)
        val subTable1 = DropTable(TableType.All, 1, listOf(item1))
        val subTable2 = DropTable(TableType.All, 1, listOf(item2))
        val root = DropTable(TableType.First, -1, listOf(subTable1, subTable2))

        val list = mutableListOf<ItemDrop>()
        root.collect(list, -1, -1)

        assertTrue(list.contains(item1))
        assertFalse(list.contains(item2))
    }

    @Test
    fun `Roll all tables of tables`() {
        val item1 = drop("1", 1)
        val item2 = drop("2", 1)
        val subTable1 = DropTable(TableType.First, 1, listOf(item1))
        val subTable2 = DropTable(TableType.First, 1, listOf(item2))
        val root = DropTable(TableType.All, -1, listOf(subTable1, subTable2))

        val list = mutableListOf<ItemDrop>()
        root.collect(list, -1, -1)

        assertTrue(list.contains(item1))
        assertTrue(list.contains(item2))
    }

    @Test
    fun `Don't collect drop with chance lower than roll`() {
        val item1 = drop("1", 10)
        val table = DropTable(TableType.First, -1, listOf(item1))

        val list = mutableListOf<ItemDrop>()
        table.collect(list, -1, 100)

        assertFalse(list.contains(item1))
    }

    private fun drop(id: String, chance: Int): ItemDrop = ItemDrop(id, 1..1, chance)
}