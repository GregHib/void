package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.config.TableDefinition
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TablesTest {

    private data class Field(
        val name: String,
        val type: ColumnType<*, *>,
        val default: Any,
    )

    @Test
    fun `Test loading a table`() {
        ItemDefinitions.set(arrayOf(ItemDefinition(id = 0, stringId = "item_id")), mapOf("item_id" to 0))
        ObjectDefinitions.set(arrayOf(ObjectDefinition(id = 0, stringId = "obj_id")), mapOf("obj_id" to 0))
        val uri = TablesTest::class.java.getResource("test-table.toml")!!
        Tables.load(listOf(uri.path))

        assertTrue(Tables.loaded)

        val definition = Tables.definitions["header"]
        assertNotNull(definition)
        assertColumns(
            listOf(
                Field("int_field", ColumnType.IntType, 0),
                Field("string_field", ColumnType.StringType, ""),
                Field("item_field", ColumnType.ItemType, -1),
                Field("obj_field", ColumnType.ObjectType, -1),
                Field("int_list", ColumnType.IntList, emptyList<Int>()),
                Field("str_list", ColumnType.StringList, emptyList<String>()),
                Field("item_list", ColumnType.ItemList, IntArray(0)),
                Field("obj_list", ColumnType.ObjectList, IntArray(0)),
                Field("int_int", ColumnType.IntIntPair, Pair(0, 0)),
                Field("str_int", ColumnType.StrIntPair, Pair("", 0)),
                Field("int_str", ColumnType.IntStrPair, Pair(0, "")),
                Field("int_int_list", ColumnType.IntIntList, emptyList<Pair<Int, Int>>()),
                Field("str_int_list", ColumnType.StrIntList, emptyList<Pair<String, Int>>()),
                Field("int_str_list", ColumnType.IntStrList, emptyList<Pair<Int, String>>()),
            ), definition
        )
        assertContentEquals(intArrayOf(0, 1), definition.rows)

        assertTrue(Rows.loaded)
        val row = Rows.getOrNull("row")
        assertNotNull(row)
        val expected = arrayOf(
            1,
            "text",
            0,
            0,
            listOf(1, 2, 3),
            listOf("one", "two"),
            intArrayOf(0),
            intArrayOf(0),
            Pair(1, 2),
            Pair("one", 2),
            Pair(1, "two"),
            listOf(Pair(1, 2), Pair(3, 4)),
            listOf(Pair("one", 2), Pair("three", 4)),
            listOf(Pair(1, "two"), Pair(3, "four")),
        )
        for (i in row.data.indices) {
            assertAnyEquals(expected[i], row.data[i]!!, i)
        }

        assertEquals(1, Tables.int("header.row.int_field"))
        assertEquals("text", Tables.string("header.row.string_field"))
        assertEquals("item_id", Tables.item("header.row.item_field"))
        assertEquals("obj_id", Tables.obj("header.row.obj_field"))
        assertEquals(listOf(1, 2, 3), Tables.intList("header.row.int_list"))
        assertEquals(listOf("one", "two"), Tables.stringList("header.row.str_list"))
        assertEquals(listOf("item_id"), Tables.itemList("header.row.item_list"))
        assertEquals(listOf("obj_id"), Tables.objList("header.row.obj_list"))
        assertEquals(Pair(1, 2), Tables.intPair("header.row.int_int"))
        assertEquals(Pair("one", 2), Tables.strIntPair("header.row.str_int"))
        assertEquals(Pair(1, "two"), Tables.intStrPair("header.row.int_str"))
        assertEquals(listOf(Pair(1, 2), Pair(3, 4)), Tables.intPairList("header.row.int_int_list"))
        assertEquals(listOf(Pair("one", 2), Pair("three", 4)), Tables.strIntList("header.row.str_int_list"))
        assertEquals(listOf(Pair(1, "two"), Pair(3, "four")), Tables.intStrList("header.row.int_str_list"))

        assertEquals(0, Tables.int("header.row_two.int_field"))
        assertEquals("", Tables.string("header.row_two.string_field"))
        assertThrows<IllegalStateException> {
            Tables.item("header.row_two.item_field")
        }
        assertEquals(emptyList(), Tables.intList("header.row_two.int_list"))
        assertEquals(emptyList(), Tables.stringList("header.row_two.str_list"))
        assertEquals(emptyList(), Tables.itemList("header.row_two.item_list"))
        assertEquals(Pair(0, 0), Tables.intPair("header.row_two.int_int"))
        assertEquals(Pair("", 0), Tables.strIntPair("header.row_two.str_int"))
        assertEquals(Pair(0, ""), Tables.intStrPair("header.row_two.int_str"))
        assertEquals(emptyList(), Tables.intPairList("header.row_two.int_int_list"))
        assertEquals(emptyList(), Tables.strIntList("header.row_two.str_int_list"))
        assertEquals(emptyList(), Tables.intStrList("header.row_two.int_str_list"))
    }

    private fun assertColumns(expected: List<Field>, definition: TableDefinition) {
        for (i in expected.indices) {
            assertAnyEquals(expected[i].default, definition.default[i], i)
            assertAnyEquals(expected[i].type, definition.types[i], i)
        }
        assertEquals(expected.mapIndexed { index, it -> it.name to index }.toMap(), definition.columns)
    }

    private fun assertAnyEquals(expect: Any, actual: Any, index: Int) {
        if (expect is IntArray && actual is IntArray) {
            assertContentEquals(expect, actual)
        } else {
            assertEquals(expect, actual, "Failed at index $index")
        }
    }
}