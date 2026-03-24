package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import kotlin.test.assertContentEquals

class ColumnTypeTest {

    @Test
    fun `Pair type`() {
        val type = ColumnType.RowPair(ColumnType.IntType, ColumnType.IntType)

        val array = arrayOfNulls<Any>(type.size)
        Config.stringReader("[1, 4]") {
            type.set(array, 0, this)
        }
        assertContentEquals(arrayOf(1, 4), array)
    }

    @Test
    fun `List type`() {
        val type = ColumnType.RowList(ColumnType.IntType)

        val array = arrayOfNulls<Any>(4)
        Config.stringReader("[1, 2, 3]") {
            type.set(array, 0, this)
        }
        assertContentEquals(arrayOf(3, 1, 2, 3), array)
    }

    @Test
    fun `Triple list type`() {
        val type = ColumnType.RowList(ColumnType.RowPair(ColumnType.StringType, ColumnType.IntType))

        val array = arrayOfNulls<Any>(3)
        Config.stringReader("[[\"test\", 3]]") {
            type.set(array, 0, this)
        }
        assertContentEquals(arrayOf(1, "test", 3), array)
    }
}