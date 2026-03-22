package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.config.TableDefinition
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TablesTest {

    private data class Field(
        val name: String,
        val type: ColumnType<*, *>,
        val default: Any
    )

    @Test
    fun `Test loading a table`() {
        ItemDefinitions.set(arrayOf(ItemDefinition()), mapOf("item_id" to 1))
        val uri = TablesTest::class.java.getResource("test-table.toml")!!
        Tables.load(listOf(uri.path))

        assertTrue(Tables.loaded)

        val definition = Tables.definitions["header"]
        assertNotNull(definition)
        println(definition)
        assertColumns(listOf(
            Field("int_field", ColumnType.IntType, 0),
            Field("string_field", ColumnType.StringType, ""),
            Field("item_field", ColumnType.ItemType, -1),
        ), definition)
        assertContentEquals(intArrayOf(0, 1), definition.rows)

        assertTrue(Rows.loaded)
        val row = Rows.getOrNull("row")
        assertNotNull(row)
        assertContentEquals(arrayOf(1, "text", 1), row.data)

        assertEquals(1, Tables.int("header.row.int_field"))
        assertEquals("text", Tables.string("header.row.string_field"))
        assertEquals("item_id", Tables.item("header.row.item_field"))

        assertEquals(0, Tables.int("header.row_two.int_field"))
        assertEquals("", Tables.string("header.row_two.string_field"))
        assertEquals("", Tables.item("header.row.item_field"))
    }

    private fun assertColumns(expected: List<Field>, definition: TableDefinition) {
        assertContentEquals(expected.map { it.default }.toTypedArray(), definition.default)
        assertContentEquals(expected.map { it.type }.toTypedArray(), definition.types)
        assertEquals(expected.mapIndexed { index, it -> it.name to index }.toMap(), definition.columns)
    }
}