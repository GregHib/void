package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.config.TableDefinition
import world.gregs.voidps.engine.data.definition.ColumnType.IntType
import world.gregs.voidps.engine.data.definition.ColumnType.ObjectType
import world.gregs.voidps.engine.data.definition.ColumnType.RowList
import world.gregs.voidps.engine.data.definition.ColumnType.RowPair
import world.gregs.voidps.engine.data.definition.ColumnType.StringType
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
        NPCDefinitions.set(arrayOf(NPCDefinition(id = 0, stringId = "npc_id")), mapOf("npc_id" to 0))
        val uri = TablesTest::class.java.getResource("test-table.toml")!!
        Tables.load(listOf(uri.path))

        assertTrue(Tables.loaded)

        val definition = Tables.definitions["header"]
        assertNotNull(definition)
        assertColumns(
            listOf(
                Field("int_field", ColumnType.IntType, 0), // int_field
                Field("string_field", StringType, ""), // string_field
                Field("item_field", ColumnType.ItemType, -1), // item_field
                Field("obj_field", ColumnType.ObjectType, -1), // obj_field
                Field("npc_field", ColumnType.NPCType, -1), // npc_field
                Field("int_list", RowList(IntType, 3), emptyList<Int>()), // int_list
                Field("str_list", RowList(StringType, 2), emptyList<String>()), // str_list
                Field("item_list", RowList(ColumnType.ItemType, 1), emptyList<Int>()), // item_list
                Field("obj_list", RowList(ObjectType, 1), emptyList<Int>()), // obj_list
                Field("npc_list", RowList(ColumnType.NPCType, 1), emptyList<Int>()), // npc_list
                Field("int_int", RowPair(IntType, IntType), 0), // int_int
                Field("str_int", RowPair(StringType, IntType), ""), // str_int
                Field("int_str", RowPair(IntType, StringType), 0), // int_str
                Field("int_int_list", ColumnType.RowList(RowPair(IntType, IntType), 2), emptyList<Pair<Int, Int>>()), // int_int_list
                Field("str_int_list", ColumnType.RowList(RowPair(StringType, IntType), 2), emptyList<Pair<String, Int>>()), // str_int_list
                Field("int_str_list", ColumnType.RowList(RowPair(IntType, StringType), 2), emptyList<Pair<Int, String>>()), // int_str_list
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
            0,
            3, 1, 2, 3, // int list
            2, "one", "two", // str list
            1, 0, // item list
            1, 0, // obj list
            1, 0, // npc list
            1, 2, // int pair
            "one", 2, // str/int pair
            1, "two", // int/str pair
            2, 1, 2, 3, 4, // int pair list
            2, "one", 2, "three", 4, // str/int list
            2, 1, "two", 3, "four", // int/str list
        )
        println(row.data.toList())
        for (i in expected.indices) {
            val expect = expected.getOrNull(i) ?: break
            val actual = row.data[i] ?: break
            println("${expect} $actual")
            assertAnyEquals(expect, actual, "${expected[i]} index ${i}")
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
        var i = 0
        println("Check ${definition.types.toList()}")
        for (field in expected) {
            assertAnyEquals(field.default, definition.default[i], "${field} index $i")
            assertAnyEquals(field.type, definition.types[i], "${field} index $i")
            assertEquals(i, definition.columns[field.name])
            i += field.type.size
        }
    }

    private fun assertAnyEquals(expect: Any?, actual: Any?, index: String) {
        if (expect is IntArray && actual is IntArray) {
            assertContentEquals(expect, actual)
        } else {
            assertEquals(expect, actual, "Failed at $index")
        }
    }
}