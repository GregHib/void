package world.gregs.voidps.engine.entity.item.drop

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DropTablesTest {

    @Test
    fun `Load from yaml`() {
        val decoder = DropTables().load("./src/test/resources/drop-table.yml")
        val table = decoder.getValue("test_drop_table")
        assertNotNull(table)
        assertNull(decoder.get("invalid_drop_table"))
    }
}